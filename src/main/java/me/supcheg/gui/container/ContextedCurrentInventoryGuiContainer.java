package me.supcheg.gui.container;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.AdventureComponentConverter;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.supcheg.gui.util.GuiUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.load.accessor.GuiAccessor;
import me.supcheg.gui.render.TypedRenderer;

import java.util.Collection;
import java.util.Collections;

public class ContextedCurrentInventoryGuiContainer<I, C> extends ContextedGuiContainer<I, C, ContextedCurrentInventoryGuiContainer.CurrentInventoryContextContainer<C>> {

    public ContextedCurrentInventoryGuiContainer(@NotNull I instance, @NotNull GuiAccessor<I> guiAccessor,
                                                 @NotNull GuiAccessor<C> contextAccessor,
                                                 boolean lockContents) {
        super(0, InventoryType.CHEST, instance, guiAccessor, contextAccessor, lockContents);
    }

    @Override
    public boolean shouldBeAddedToHistory() {
        return false;
    }

    @Override
    @NotNull
    protected CurrentInventoryContextContainer<C> newContextContainer(@NotNull Player player) {
        return new CurrentInventoryContextContainer<>(this, player);
    }

    @Override
    public void tick() {
        for (CurrentInventoryContextContainer<C> container : uniqueId2contextContainer.values()) {
            Inventory inventory = container.getInventory();
            if (inventory != null && !inventory.getViewers().isEmpty()) {
                tick(container, inventory);
            }
        }
    }

    @Override
    public void acceptInventoryEvent(@NotNull Player player, @NotNull InventoryEvent event) {
        CurrentInventoryContextContainer<C> contextContainer = getContextContainer(player);
        if (contextContainer == null) {
            return;
        }
        contextContainer.acceptInventoryEvent(event);

        lockContents(event);

        if (event instanceof InventoryCloseEvent) {
            if (!contextContainer.firstClose) {
                invalidateContextContainer(player);
            } else {
                contextContainer.firstClose = false;
            }

        } else if (contextContainer.inPlayerInventory) {
            Bukkit.getScheduler().runTask(GuiUtil.plugin(), () -> {
                if (player.isOnline() && player.getOpenInventory().getType() != InventoryType.CRAFTING) {
                    invalidateContextContainer(player);
                    player.setItemOnCursor(player.getItemOnCursor());
                }
            });
        }
    }

    @Override
    public void acceptPacketEvent(@NotNull PacketEvent event) {
        CurrentInventoryContextContainer<C> contextContainer = getContextContainer(event.getPlayer());
        if (contextContainer != null) {
            contextContainer.acceptPacketEvent(event);
        }
    }

    public static class CurrentInventoryContextContainer<C> extends ContextContainer<C> {
        private boolean inPlayerInventory;
        private boolean firstClose;

        public CurrentInventoryContextContainer(@NotNull ContextedGuiContainer<?, C, ?> guiContainer, @NotNull Player player) {
            super(guiContainer, player);
        }

        @Override
        public void open(@NotNull Player player) {
            Inventory inventory = player.getOpenInventory().getTopInventory();

            if (inventory.getType() != InventoryType.CRAFTING) {
                inPlayerInventory = false;
                firstClose = true;
                player.openInventory(inventory);
            } else {
                inPlayerInventory = true;
                firstClose = false;
            }
        }

        @Override
        public void acceptInventoryEvent(@NotNull InventoryEvent event) {
            guiContainer.contextAccessor.acceptInventoryEvent(context, event, Collections.emptyList());
        }

        @Override
        public void acceptPacketEvent(@NotNull PacketEvent event) {
            if (!inPlayerInventory && event.getPacketType() == PacketType.Play.Server.OPEN_WINDOW) {
                var chatComponents = event.getPacket().getChatComponents();

                WrappedChatComponent wrapped = chatComponents.read(0);

                Component title = guiContainer.contextAccessor
                        .createTitle(context, AdventureComponentConverter.fromWrapper(wrapped));

                if (title != null) {
                    chatComponents.write(0, AdventureComponentConverter.fromComponent(title));
                }
            }

            guiContainer.contextAccessor.acceptPacketEvent(context, event, Collections.emptyList());
        }

        @NotNull
        @Override
        public Collection<TypedRenderer> newRenderers() {
            return Collections.emptyList();
        }

        @Override
        public void acceptRenderers(@NotNull Collection<TypedRenderer> renderers) {
            // Чужой инвентарь не должен никак изменяться
        }
    }
}
