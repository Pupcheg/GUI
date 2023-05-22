package me.supcheg.gui.container;

import com.comphenix.protocol.events.PacketEvent;
import lombok.Getter;
import me.supcheg.gui.util.GuiMap;
import me.supcheg.gui.load.accessor.GuiAccessor;
import me.supcheg.gui.util.GuiUtil;
import me.supcheg.gui.render.TypedRenderer;
import me.supcheg.gui.tick.GuiAnimation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public abstract class ContextedGuiContainer<I, C, H extends ContextedGuiContainer.ContextContainer<C>> extends GuiContainer<I> {

    protected final GuiAccessor<C> contextAccessor;

    protected final Map<UUID, H> uniqueId2contextContainer;
    protected final Map<UUID, H> uniqueId2nextContextContainer;

    protected ContextedGuiContainer(int size, @NotNull InventoryType inventoryType,
                                    @NotNull I instance, @NotNull GuiAccessor<I> guiAccessor,
                                    @NotNull GuiAccessor<C> contextAccessor,
                                    boolean lockContents) {
        super(size, inventoryType, instance, guiAccessor, lockContents);
        this.contextAccessor = contextAccessor;
        this.uniqueId2contextContainer = new HashMap<>();
        this.uniqueId2nextContextContainer = new HashMap<>();
    }

    @Override
    public void open(@NotNull Player player) {
        GuiMap.put(player, this);
        createContextContainer(player).open(player);
    }

    protected void tick(@NotNull ContextedGuiContainer.ContextContainer<C> contextContainer, @NotNull Inventory inventory) {
        contextContainer.acceptRenderers(contextAccessor.tick(contextContainer.getContext(), contextContainer.getRenderers()));

        if (!inventory.getViewers().isEmpty()) {
            for (GuiAnimation guiAnimation : contextContainer.getAnimations()) {
                try {
                    guiAnimation.tick(inventory::setItem);
                } catch (Exception ex) {
                    GuiUtil.logger().error("An error occurred while ticking {} to gui: {}", guiAnimation, guiInstance, ex);
                }
            }
        }
    }

    @NotNull
    protected H createContextContainer(@NotNull Player player) {
        UUID uniqueId = player.getUniqueId();
        H contextContainer = newContextContainer(player);

        if (uniqueId2contextContainer.containsKey(uniqueId)) {
            uniqueId2nextContextContainer.put(uniqueId, contextContainer);
        } else {
            uniqueId2contextContainer.put(uniqueId, contextContainer);
        }

        return contextContainer;
    }

    @Nullable
    protected H getContextContainer(@NotNull Player player) {
        return uniqueId2contextContainer.get(player.getUniqueId());
    }

    protected void invalidateContextContainer(@NotNull Player player) {
        UUID uniqueId = player.getUniqueId();

        GuiMap.remove(player, this);
        uniqueId2contextContainer.remove(uniqueId);

        var nextContainer = uniqueId2nextContextContainer.get(uniqueId);
        if (nextContainer != null) {
            uniqueId2contextContainer.put(uniqueId, nextContainer);
        }

    }

    @NotNull
    @Contract("_ -> new")
    protected abstract H newContextContainer(@NotNull Player player);

    @Getter
    public abstract static class ContextContainer<C> {
        protected Collection<TypedRenderer> renderers;
        protected final ContextedGuiContainer<?, C, ?> guiContainer;
        protected final UUID uniqueId;
        protected final C context;
        protected final Set<GuiAnimation> animations;
        protected Inventory inventory;

        protected ContextContainer(@NotNull ContextedGuiContainer<?, C, ?> guiContainer, @NotNull Player player) {
            this.guiContainer = guiContainer;
            this.uniqueId = player.getUniqueId();

            // guiInstance needed for sealed classes
            this.context = guiContainer.contextAccessor.newInstance(guiContainer, guiContainer.guiInstance, player);
            this.animations = new HashSet<>();
        }

        public void open(@NotNull Player player) {
            this.inventory = guiContainer.createInventory(guiContainer.contextAccessor.createTitle(context));
            render();
            player.openInventory(inventory);
        }

        @Nullable
        public Player getPlayer() {
            return Bukkit.getPlayer(uniqueId);
        }

        public void render() {
            acceptRenderers(guiContainer.contextAccessor.appendRenderers(context, getRenderers()));
        }

        public void acceptInventoryEvent(@NotNull InventoryEvent event) {
            Collection<TypedRenderer> renderers = inventory != null ? getRenderers() : Collections.emptyList();
            acceptRenderers(guiContainer.contextAccessor.acceptInventoryEvent(context, event, renderers));
        }

        public void acceptPacketEvent(@NotNull PacketEvent event) {
            Collection<TypedRenderer> renderers = inventory != null ? getRenderers() : Collections.emptyList();
            acceptRenderers(guiContainer.contextAccessor.acceptPacketEvent(context, event, renderers));
        }


        @NotNull
        public Collection<TypedRenderer> getRenderers() {
            if (renderers == null) {
                renderers = newRenderers();
            }
            return renderers;
        }

        @NotNull
        public abstract Collection<TypedRenderer> newRenderers();

        public abstract void acceptRenderers(@NotNull Collection<TypedRenderer> renderers);

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof ContextedGuiContainer.ContextContainer<?> that)) {
                return false;
            }

            return uniqueId.equals(that.uniqueId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uniqueId);
        }
    }

}
