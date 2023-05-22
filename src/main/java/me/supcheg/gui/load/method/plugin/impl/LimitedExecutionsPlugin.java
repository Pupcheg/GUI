package me.supcheg.gui.load.method.plugin.impl;

import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.annotation.Limit;
import me.supcheg.gui.load.method.plugin.MethodPlugin;
import me.supcheg.gui.reflection.ArgumentsResolver;

import java.util.Map;
import java.util.WeakHashMap;

public class LimitedExecutionsPlugin extends MethodPlugin {

    private final int limit;
    private final Map<Object, Integer> leftExecutionsMap;

    public LimitedExecutionsPlugin(@NotNull Limit annotation) {
        super(annotation);

        this.limit = annotation.value();
        this.leftExecutionsMap = new WeakHashMap<>();
    }

    @Override
    public boolean run(@NotNull Object guiInstance, @NotNull ArgumentsResolver argumentsResolver) {
        int leftExecutions = leftExecutionsMap.getOrDefault(guiInstance, limit);

        if (leftExecutions > 0) {
            leftExecutionsMap.put(guiInstance, leftExecutions - 1);
            return true;
        } else {
            return false;
        }
    }
}
