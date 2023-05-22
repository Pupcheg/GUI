package me.supcheg.gui.container;

import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.annotation.Renderer;
import me.supcheg.gui.load.accessor.GuiAccessor;
import me.supcheg.gui.render.InventoryRenderer;
import me.supcheg.gui.render.TypedRenderer;

import java.util.Collection;
import java.util.Collections;

public class ContextedStandardGuiContainer<I, C> extends ContextedGuiContainer<I, C, ContextedStandardGuiContainer.StandardContextContainer<C>> {

    public ContextedStandardGuiContainer(int size, @NotNull InventoryType inventoryType,
                                         @NotNull I instance, @NotNull GuiAccessor<I> guiAccessor,
                                         @NotNull GuiAccessor<C> contextAccessor,
                                         boolean lockContents) {
        super(size, inventoryType, instance, guiAccessor, contextAccessor, lockContents);
    }

    @Override
    @NotNull
    protected StandardContextContainer<C> newContextContainer(@NotNull Player player) {
        return new StandardContextContainer<>(this, player);
    }

    @Override
    public void tick() {
        for (StandardContextContainer<C> container : uniqueId2contextContainer.values()) {
            Inventory inventory = container.getInventory();
            if (inventory != null && !inventory.getViewers().isEmpty()) {
                tick(container, inventory);
            }
        }
    }

    @Override
    public void acceptInventoryEvent(@NotNull Player player, @NotNull InventoryEvent event) {

        StandardContextContainer<C> contextContainer = getContextContainer(player);
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
        }
    }

    @Override
    public void acceptPacketEvent(@NotNull PacketEvent event) {
        var contextContainer = getContextContainer(event.getPlayer());
        if (contextContainer != null) {
            contextContainer.acceptPacketEvent(event);
        }
    }


    public static class StandardContextContainer<C> extends ContextContainer<C> {

        public StandardContextContainer(ContextedStandardGuiContainer<?, C> guiContainer, @NotNull Player player) {
            super(guiContainer, player);
        }

        @NotNull
        @Override
        public Collection<TypedRenderer> newRenderers() {
            return Collections.singleton(new InventoryRenderer(Renderer.INVENTORY, inventory.getContents(), animations));
        }

        @Override
        public void acceptRenderers(@NotNull Collection<TypedRenderer> renderers) {
            for (TypedRenderer renderer : renderers) {
                if (renderer.getType() == Renderer.INVENTORY) {
                    renderer.asInventoryRenderer().renderContents(inventory::setContents);
                }
            }
        }
    }
}
