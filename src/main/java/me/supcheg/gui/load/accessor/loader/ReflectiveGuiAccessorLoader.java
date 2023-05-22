package me.supcheg.gui.load.accessor.loader;

import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.load.accessor.ReflectiveGuiAccessor;
import me.supcheg.gui.load.accessor.ReflectivePacketGuiAccessor;
import me.supcheg.gui.reflection.DeepClassScanner;

public class ReflectiveGuiAccessorLoader<T> extends AbstractReflectiveGuiAccessorLoader<T, ReflectiveGuiAccessor.Builder<T, ReflectivePacketGuiAccessor<T>>> {
    public ReflectiveGuiAccessorLoader(@NotNull DeepClassScanner<T> clazz) {
        super(clazz);
    }

    @Override
    protected ReflectiveGuiAccessor.Builder<T, ReflectivePacketGuiAccessor<T>> newBuilder() {
        return new ReflectiveGuiAccessor.Builder<>();
    }
}
