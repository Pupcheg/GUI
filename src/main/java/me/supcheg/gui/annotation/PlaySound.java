package me.supcheg.gui.annotation;

import net.kyori.adventure.sound.Sound;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PlaySound {

    @NotNull
    @Pattern("([a-z0-9_\\-.]+:)?[a-z0-9_\\-./]+")
    String value();

    @NotNull
    Sound.Source source() default Sound.Source.MASTER;

    float volume() default 0.125f;

    float pitch() default 1f;

    @NotNull
    Priority priority() default Priority.LOW;

    @NotNull
    PointCut pointCut() default PointCut.AFTER;
}
