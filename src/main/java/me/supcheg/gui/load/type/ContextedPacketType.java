package me.supcheg.gui.load.type;

import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.annotation.IncludePlayerInventory;
import me.supcheg.gui.container.ContextedPacketGuiContainer;
import me.supcheg.gui.container.GuiContainer;
import me.supcheg.gui.load.accessor.GuiAccessor;
import me.supcheg.gui.load.accessor.loader.GuiAccessorLoaders;
import me.supcheg.gui.reflection.DeepClassScanner;

public class ContextedPacketType extends ContextedType {
    public ContextedPacketType() {
    }

    @Override
    public boolean test(@NotNull DeepClassScanner<?> contextClazz) {
        return contextClazz.isAnnotationPresent(IncludePlayerInventory.class);
    }

    @NotNull
    @Override
    public <I> GuiContainer<I> newContextedContainer(int size, @NotNull InventoryType inventoryType,
                                                     I instance, @NotNull GuiAccessor<I> guiAccessor,
                                                     @NotNull GuiAccessor<?> contextAccessor,
                                                     boolean lockContents) {
        return new ContextedPacketGuiContainer<>(size, inventoryType, instance, guiAccessor, contextAccessor, lockContents);
    }

    @NotNull
    @Override
    public <I> GuiAccessor<I> loadGuiAccessor(@NotNull DeepClassScanner<I> clazz) {
        return GuiAccessorLoaders.loadReflectivePacket(clazz);
    }
}
