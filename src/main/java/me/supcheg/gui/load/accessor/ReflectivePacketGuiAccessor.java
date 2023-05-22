package me.supcheg.gui.load.accessor;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.supcheg.gui.load.method.SlotClickMethod;
import me.supcheg.gui.util.GuiUtil;
import me.supcheg.gui.reflection.ArgumentsResolver;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class ReflectivePacketGuiAccessor<T> extends ReflectiveGuiAccessor<T> {

    protected final Int2ObjectMap<Collection<SlotClickMethod>> playerSlot2handlers;

    @Contract(pure = true)
    protected ReflectivePacketGuiAccessor(@NotNull Builder<T> builder) {
        super(builder);
        this.playerSlot2handlers = Objects.requireNonNull(builder.playerSlot2handlers);
    }

    @Override
    protected boolean acceptSlotClick(@NotNull T instance, @NotNull InventoryClickEvent clickEvent, @NotNull ArgumentsResolver currentResolver, boolean onlyDoubleClick) {
        if (!playerSlot2handlers.isEmpty()) {
            int slot = clickEvent.getSlot();
            Inventory clickedInventory = clickEvent.getClickedInventory();

            if (clickedInventory != null && clickedInventory.getType() == InventoryType.PLAYER) {
                var currentSlotMethods = playerSlot2handlers.get(slot);
                boolean handled = false;

                if (currentSlotMethods != null) {
                    for (var currentSlotMethod : currentSlotMethods) {
                        if (onlyDoubleClick && !currentSlotMethod.shouldHandleDoubleClick()) {
                            continue;
                        }
                        try {
                            handled |= currentSlotMethod.invokeWithPlugins(instance, currentResolver);
                        } catch (Exception ex) {
                            GuiUtil.logger().error("An error occurred while accepting click to a gui {} at slot: {}",
                                    instance, slot, ex);
                        }
                    }
                    return handled;
                }
            }
        }

        return super.acceptSlotClick(instance, clickEvent, currentResolver, onlyDoubleClick);
    }


    public static class Builder<T> extends ReflectiveGuiAccessor.Builder<T, ReflectivePacketGuiAccessor<T>> {
        private Int2ObjectMap<Collection<SlotClickMethod>> playerSlot2handlers;

        public Builder() {
        }

        public Builder<T> playerSlot2handlers(Int2ObjectMap<Collection<SlotClickMethod>> playerSlot2handlers) {
            this.playerSlot2handlers = playerSlot2handlers;
            return this;
        }

        @Override
        public ReflectivePacketGuiAccessor<T> build() {
            return new ReflectivePacketGuiAccessor<>(this);
        }
    }
}
