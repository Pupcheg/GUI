package me.supcheg.gui.container;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import lombok.Getter;
import lombok.Setter;
import me.supcheg.gui.util.GuiUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import me.supcheg.gui.annotation.Renderer;
import me.supcheg.gui.load.accessor.GuiAccessor;
import me.supcheg.gui.render.CombinedInventoryRenderer;
import me.supcheg.gui.render.InventoryRenderer;
import me.supcheg.gui.render.TypedRenderer;
import me.supcheg.gui.tick.GuiAnimation;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ContextedPacketGuiContainer<I, C> extends ContextedGuiContainer<I, C, ContextedPacketGuiContainer.PacketContextContainer<C>> {

    public static final int PLAYER_INVENTORY_ID = 0;
    public static final int CURSOR_INVENTORY_ID = -1;
    public static final int VISIBLE_PLAYER_INVENTORY_SIZE = 9 * 4;
    private static final ItemStack AIR = new ItemStack(Material.AIR);

    public ContextedPacketGuiContainer(int size, @NotNull InventoryType inventoryType,
                                       @NotNull I instance, @NotNull GuiAccessor<I> guiAccessor,
                                       @NotNull GuiAccessor<C> contextAccessor,
                                       boolean lockContents) {
        super(size, inventoryType, instance, guiAccessor, contextAccessor, lockContents);
    }

    @NotNull
    public static List<@NotNull ItemStack> preparePlayerContents(@Nullable ItemStack @NotNull [] itemStacks) {
        ItemStack[] withoutNulls = new ItemStack[itemStacks.length + 9];

        System.arraycopy(itemStacks, 9, withoutNulls, 9, itemStacks.length - 9);
        System.arraycopy(itemStacks, 0, withoutNulls, withoutNulls.length - 9, 9);

        for (int i = 0; i < withoutNulls.length; i++) {
            if (withoutNulls[i] == null) {
                withoutNulls[i] = AIR;
            }
        }

        return Arrays.asList(withoutNulls);
    }

    @Override
    @NotNull
    protected PacketContextContainer<C> newContextContainer(@NotNull Player player) {
        return new PacketContextContainer<>(this, player);
    }

    @Override
    public void tick() {
        for (PacketContextContainer<C> contextContainer : uniqueId2contextContainer.values()) {
            Inventory inventory = contextContainer.getInventory();
            if (inventory != null && !inventory.getViewers().isEmpty()) {
                tickAnimations(contextContainer);
                tickMethods(contextContainer);
            }
        }
    }

    private void tickMethods(@NotNull PacketContextContainer<C> contextContainer) {
        contextContainer.acceptRenderers(contextAccessor.tick(contextContainer.getContext(), contextContainer.getRenderers()));
    }

    private void tickAnimations(@NotNull PacketContextContainer<C> contextContainer) {
        Player player = contextContainer.getPlayer();
        if (player != null) {
            Inventory inventory = contextContainer.getInventory();
            for (GuiAnimation guiAnimation : contextContainer.getAnimations()) {
                guiAnimation.tick(inventory::setItem);
            }

            ItemStack[] playerContents = contextContainer.getPlayerContents();
            if (playerContents != null) {
                boolean anyTicked = false;
                for (GuiAnimation guiAnimation : contextContainer.getPlayerAnimations()) {
                    anyTicked |= guiAnimation.tick((slot, itemStack) -> playerContents[slot] = itemStack);
                }

                if (anyTicked) {
                    sendPlayerInventoryContents(player, contextContainer);
                }
            }

        }
    }

    @Override
    public void acceptInventoryEvent(@NotNull Player player, @NotNull InventoryEvent event) {
        PacketContextContainer<C> contextContainer = getContextContainer(player);
        if (contextContainer == null) {
            return;
        }

        contextContainer.acceptInventoryEvent(event);
        lockContents(event);

        if (event instanceof InventoryCloseEvent) {
            if (lockContents) {
                contextContainer.getInventory().clear();
            }
            invalidateContextContainer(player);

            Bukkit.getScheduler().runTask(GuiUtil.plugin(), () -> {
                if (player.isOnline()) {
                    PlayerInventory playerInventory = player.getInventory();
                    playerInventory.setContents(playerInventory.getContents());
                }
            });
        }
    }

    @Override
    public void acceptPacketEvent(@NotNull PacketEvent event) {
        PacketContextContainer<C> contextContainer = getContextContainer(event.getPlayer());
        if (contextContainer == null) {
            return;
        }

        lockContents(contextContainer, event);
        contextContainer.acceptPacketEvent(event);
    }

    private void lockContents(@NotNull PacketContextContainer<C> contextContainer, @NotNull PacketEvent event) {
        if (!lockContents) {
            return;
        }

        PacketType packetType = event.getPacketType();

        if (packetType == PacketType.Play.Server.WINDOW_ITEMS || packetType == PacketType.Play.Server.SET_SLOT) {
            PacketContainer packet = event.getPacket();
            var integers = packet.getIntegers();
            int windowId = integers.read(0);

            if (windowId != PLAYER_INVENTORY_ID && windowId != CURSOR_INVENTORY_ID) {
                return;
            }

            if (packetType == PacketType.Play.Server.WINDOW_ITEMS) {
                if (packet.getMeta("submit").isEmpty()) {
                    packet.getItemListModifier().write(0, preparePlayerContents(contextContainer.getPlayerContents()));
                    packet.getItemModifier().write(0, null);
                }
            } else {
                if (windowId == CURSOR_INVENTORY_ID) {
                    packet.getItemModifier().write(0, null);
                } else {
                    event.setCancelled(true);
                }
                sendPlayerInventoryContents(event.getPlayer(), contextContainer);
            }
        }

    }

    private void sendPlayerInventoryContents(@NotNull Player player, @NotNull PacketContextContainer<C> container) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.WINDOW_ITEMS);
        packet.getIntegers()
                .write(0, ContextedPacketGuiContainer.PLAYER_INVENTORY_ID)
                .write(1, -1);
        packet.getItemListModifier()
                .write(0, preparePlayerContents(container.getPlayerContents()));
        packet.getItemModifier()
                .write(0, null);

        packet.setMeta("submit", true);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }

    @Getter
    @Setter
    public static class PacketContextContainer<C> extends ContextContainer<C> {

        private final Set<GuiAnimation> playerAnimations;
        private ContextedPacketGuiContainer<?, C> guiContainer;
        private ItemStack[] playerContents;

        public PacketContextContainer(@NotNull ContextedPacketGuiContainer<?, C> guiContainer, @NotNull Player player) {
            super(guiContainer, player);
            this.guiContainer = guiContainer;
            this.playerAnimations = new HashSet<>();
            this.playerContents = new ItemStack[VISIBLE_PLAYER_INVENTORY_SIZE];
        }

        @NotNull
        @Override
        public Collection<TypedRenderer> newRenderers() {
            ItemStack[] contents = inventory.getContents();
            return List.of(
                    new InventoryRenderer(Renderer.INVENTORY, contents, animations),
                    new InventoryRenderer(Renderer.PLAYER, playerContents, playerAnimations),
                    new CombinedInventoryRenderer(contents, playerContents, animations, playerAnimations)
            );
        }

        @Override
        public void acceptRenderers(@NotNull Collection<TypedRenderer> renderers) {
            Player player = getPlayer();
            if (player == null) {
                return;
            }

            InventoryRenderer topInventoryRenderer = null;
            Set<Renderer> types = EnumSet.noneOf(Renderer.class);
            for (TypedRenderer renderer : renderers) {
                Renderer type = renderer.getType();
                if (type == Renderer.COMBINED || type == Renderer.INVENTORY) {
                    topInventoryRenderer = renderer.asInventoryRenderer();
                }
                types.add(type);
            }

            if (types.contains(Renderer.COMBINED)) {
                Objects.requireNonNull(topInventoryRenderer);

                topInventoryRenderer.renderContents(inventory::setContents);
                guiContainer.sendPlayerInventoryContents(player, this);
            } else {
                if (types.contains(Renderer.INVENTORY)) {
                    Objects.requireNonNull(topInventoryRenderer);

                    topInventoryRenderer.renderContents(inventory::setContents);
                }

                if (types.contains(Renderer.PLAYER)) {
                    guiContainer.sendPlayerInventoryContents(player, this);
                }
            }

        }
    }
}
