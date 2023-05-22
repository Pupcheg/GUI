package me.supcheg.gui.load.accessor.loader;

import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.load.accessor.GuiAccessor;
import me.supcheg.gui.reflection.DeepClassScanner;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class GuiAccessorLoaders {

    private static final Map<DeepClassScanner<?>, GuiAccessor<?>> REFLECTIVE_CACHE = new HashMap<>();
    private static final Function<DeepClassScanner<?>, GuiAccessor<?>> REFLECTIVE_MAPPING =
            deepClassScanner -> new ReflectiveGuiAccessorLoader<>(deepClassScanner).load();

    private static final Map<DeepClassScanner<?>, GuiAccessor<?>> SIMPLE_REFLECTIVE_CACHE = new HashMap<>();
    private static final Function<DeepClassScanner<?>, GuiAccessor<?>> SIMPLE_REFLECTIVE_MAPPING =
            deepClassScanner -> new ReflectiveSimpleGuiAccessorLoader<>(deepClassScanner).load();

    private static final Map<DeepClassScanner<?>, GuiAccessor<?>> REFLECTIVE_PACKET_CACHE = new HashMap<>();
    private static final Function<DeepClassScanner<?>, GuiAccessor<?>> REFLECTIVE_PACKET_MAPPING =
            deepClassScanner -> new ReflectivePacketGuiAccessorLoader<>(deepClassScanner).load();

    @NotNull
    @SuppressWarnings("unchecked")
    public static <I> GuiAccessor<I> loadReflective(@NotNull DeepClassScanner<I> clazzScanner) {
        return (GuiAccessor<I>) REFLECTIVE_CACHE.computeIfAbsent(clazzScanner, REFLECTIVE_MAPPING);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <I> GuiAccessor<I> loadReflectivePacket(@NotNull DeepClassScanner<I> clazzScanner) {
        return (GuiAccessor<I>) REFLECTIVE_PACKET_CACHE.computeIfAbsent(clazzScanner, REFLECTIVE_PACKET_MAPPING);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <I> GuiAccessor<I> loadSimpleReflective(@NotNull DeepClassScanner<I> clazzScanner) {
        return (GuiAccessor<I>) SIMPLE_REFLECTIVE_CACHE.computeIfAbsent(clazzScanner, SIMPLE_REFLECTIVE_MAPPING);
    }
}
