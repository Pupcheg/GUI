package me.supcheg.gui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Limit {
    int value();

    @NotNull
    Priority priority() default Priority.HIGH;

    @NotNull
    PointCut pointCut() default PointCut.BEFORE;
}
