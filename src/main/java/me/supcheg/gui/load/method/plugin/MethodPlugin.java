package me.supcheg.gui.load.method.plugin;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.annotation.PointCut;
import me.supcheg.gui.annotation.Priority;
import me.supcheg.gui.reflection.ArgumentsResolver;

import java.lang.annotation.Annotation;

public abstract class MethodPlugin implements Comparable<MethodPlugin> {

    private final Priority priority;
    private final PointCut pointCut;

    protected MethodPlugin(@NotNull Annotation annotation) {
        this.priority = getPriority(annotation);
        this.pointCut = getPointCut(annotation);
    }

    @NotNull
    public Priority getPriority() {
        return priority;
    }

    @NotNull
    public PointCut getPointCut() {
        return pointCut;
    }

    @SneakyThrows
    @NotNull
    public static Priority getPriority(@NotNull Annotation annotation) {
        try {
            return (Priority) annotation.getClass().getMethod("priority").invoke(annotation);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(annotation.getClass() + " doesn't have a 'priority' method", e);
        }
    }

    @SneakyThrows
    @NotNull
    public static PointCut getPointCut(@NotNull Annotation annotation) {
        try {
            return (PointCut) annotation.getClass().getMethod("pointCut").invoke(annotation);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(annotation.getClass() + " doesn't have a 'pointcut' method", e);
        }
    }

    public abstract boolean run(@NotNull Object guiInstance, @NotNull ArgumentsResolver argumentsResolver);

    @Override
    public int compareTo(@NotNull MethodPlugin o) {
        return priority.compareTo(o.priority);
    }
}
