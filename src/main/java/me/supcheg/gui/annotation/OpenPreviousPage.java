package me.supcheg.gui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpenPreviousPage {
    @NotNull
    Priority priority() default Priority.NORMAL;

    @NotNull
    PointCut pointCut() default PointCut.AFTER;
}
