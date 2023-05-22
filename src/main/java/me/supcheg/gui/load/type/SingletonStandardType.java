package me.supcheg.gui.load.type;

import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.container.GuiContainer;
import me.supcheg.gui.container.SingletonStandardGuiContainer;
import me.supcheg.gui.load.accessor.GuiAccessor;
import me.supcheg.gui.reflection.DeepClassScanner;

public class SingletonStandardType extends SingletonType {
    public SingletonStandardType() {
    }

    @Override
    public boolean test(@NotNull DeepClassScanner<?> mainClazz) {
        return true;
    }

    @NotNull
    @Override
    public <I> GuiContainer<I> newSingletonContainer(int size, @NotNull InventoryType inventoryType,
                                                     @NotNull I instance, @NotNull GuiAccessor<I> guiAccessor,
                                                     boolean lockContents) {
        return new SingletonStandardGuiContainer<>(size, inventoryType, instance, guiAccessor, lockContents);
    }
}
