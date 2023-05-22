package me.supcheg.gui.load.type;

import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.annotation.Paged;
import me.supcheg.gui.container.GuiContainer;
import me.supcheg.gui.container.SingletonPagedGuiContainer;
import me.supcheg.gui.load.accessor.GuiAccessor;
import me.supcheg.gui.reflection.DeepClassScanner;

public class SingletonPagedType extends SingletonType {
    public SingletonPagedType() {
    }

    @Override
    public boolean test(@NotNull DeepClassScanner<?> mainClazz) {
        return mainClazz.isAnnotationPresent(Paged.class);
    }

    @NotNull
    @Override
    public <I> GuiContainer<I> newSingletonContainer(int size, @NotNull InventoryType inventoryType,
                                                     @NotNull I instance, @NotNull GuiAccessor<I> guiAccessor,
                                                     boolean lockContents) {
        return new SingletonPagedGuiContainer<>(size, inventoryType, instance, guiAccessor, lockContents);
    }
}
