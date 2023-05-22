package me.supcheg.gui.load.type;

import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.container.ContextedStandardGuiContainer;
import me.supcheg.gui.container.GuiContainer;
import me.supcheg.gui.load.accessor.GuiAccessor;
import me.supcheg.gui.reflection.DeepClassScanner;

public class ContextedStandardType extends ContextedType {
    public ContextedStandardType() {
    }

    @Override
    public boolean test(@NotNull DeepClassScanner<?> contextClazz) {
        return true;
    }

    @NotNull
    @Override
    public <I> GuiContainer<I> newContextedContainer(int size, @NotNull InventoryType inventoryType,
                                                     I instance, @NotNull GuiAccessor<I> guiAccessor,
                                                     @NotNull GuiAccessor<?> contextAccessor,
                                                     boolean lockContents) {
        return new ContextedStandardGuiContainer<>(size, inventoryType, instance, guiAccessor, contextAccessor, lockContents);
    }
}
