package me.supcheg.gui.load.accessor.loader;

import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.load.accessor.GuiAccessor;
import me.supcheg.gui.load.accessor.ReflectivePacketGuiAccessor;
import me.supcheg.gui.load.accessor.ReflectiveSimpleGuiAccessor;
import me.supcheg.gui.reflection.DeepClassScanner;

public class ReflectiveSimpleGuiAccessorLoader<C> extends AbstractReflectiveGuiAccessorLoader<C, ReflectivePacketGuiAccessor.Builder<C>> {
    public ReflectiveSimpleGuiAccessorLoader(@NotNull DeepClassScanner<C> clazz) {
        super(clazz);
    }

    @Override
    public GuiAccessor<C> load() {
        return new ReflectiveSimpleGuiAccessor<>(getConstructor(), getContainerFields());
    }

    @Override
    protected ReflectivePacketGuiAccessor.Builder<C> newBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void appendBuilder(@NotNull ReflectivePacketGuiAccessor.Builder<C> builder) {
        throw new UnsupportedOperationException();
    }
}
