package me.supcheg.gui.annotation;

import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Gui {
    boolean lockContents() default true;

    @NotNull
    InventoryType inventoryType() default InventoryType.CHEST;

    int size() default 27;
}
