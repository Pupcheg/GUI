package me.supcheg.gui.load.accessor.loader;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.container.ContextedPacketGuiContainer;
import me.supcheg.gui.load.accessor.ReflectivePacketGuiAccessor;
import me.supcheg.gui.load.method.SlotClickMethod;
import me.supcheg.gui.reflection.DeepClassScanner;

import java.util.Collection;

public class ReflectivePacketGuiAccessorLoader<T> extends AbstractReflectiveGuiAccessorLoader<T, ReflectivePacketGuiAccessor.Builder<T>> {

    public ReflectivePacketGuiAccessorLoader(@NotNull DeepClassScanner<T> clazz) {
        super(clazz);
    }

    @Override
    protected ReflectivePacketGuiAccessor.Builder<T> newBuilder() {
        return new ReflectivePacketGuiAccessor.Builder<>();
    }

    @Override
    protected void appendBuilder(@NotNull ReflectivePacketGuiAccessor.Builder<T> builder) {
        super.appendBuilder(builder);
        builder.playerSlot2handlers(getPlayerSlotToHandlers(builder));
    }

    protected Int2ObjectMap<Collection<SlotClickMethod>> getPlayerSlotToHandlers(@NotNull ReflectivePacketGuiAccessor.Builder<T> builder) {
        var slot2handlers = builder.getSlot2handlers();
        if (slot2handlers == null) {
            return Int2ObjectMaps.emptyMap();
        }

        Int2ObjectOpenHashMap<Collection<SlotClickMethod>> playerSlot2handlers = new Int2ObjectOpenHashMap<>();

        int inventorySize = getInventorySize();

        var it = slot2handlers.int2ObjectEntrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            int slot = entry.getIntKey();
            var methods = entry.getValue();

            if (slot >= inventorySize) {
                it.remove();
                playerSlot2handlers.put(slot - inventorySize, methods);
            }
        }

        return playerSlot2handlers.isEmpty() ? Int2ObjectMaps.emptyMap() : playerSlot2handlers;
    }

    @Override
    protected int getFullSize() {
        return getInventorySize() + ContextedPacketGuiContainer.VISIBLE_PLAYER_INVENTORY_SIZE;
    }
}
