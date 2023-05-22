package me.supcheg.gui.annotation;

import me.supcheg.gui.util.IntRangeParser;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SlotClickHandler.Handlers.class)
public @interface SlotClickHandler {

    int[] value() default {};

    @NotNull
    @Pattern(IntRangeParser.PATTERN)
    String range() default "";


    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Handlers {
        SlotClickHandler[] value();
    }
}
