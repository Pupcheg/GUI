package me.supcheg.gui.load.type;

import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import me.supcheg.gui.container.GuiContainer;
import me.supcheg.gui.load.accessor.GuiAccessor;
import me.supcheg.gui.load.accessor.loader.GuiAccessorLoaders;
import me.supcheg.gui.reflection.DeepClassScanner;

public abstract class GuiType {
    protected final Group group;

    protected GuiType(@NotNull Group group) {
        this.group = group;
    }

    @NotNull
    public Group getGroup() {
        return group;
    }

    @NotNull
    public abstract <I> GuiContainer<I> newSingletonContainer(int size, @NotNull InventoryType inventoryType,
                                                              @NotNull I instance, @NotNull GuiAccessor<I> guiAccessor,
                                                              boolean lockContents);

    @NotNull
    public abstract <I> GuiContainer<I> newContextedContainer(int size, @NotNull InventoryType inventoryType,
                                                              I instance, @NotNull GuiAccessor<I> guiAccessor,
                                                              @NotNull GuiAccessor<?> contextAccessor,
                                                              boolean lockContents);

    @NotNull
    public <I> GuiAccessor<I> loadGuiAccessor(@NotNull DeepClassScanner<I> clazz) {
        return GuiAccessorLoaders.loadReflective(clazz);
    }

    public abstract boolean test(@NotNull DeepClassScanner<?> mainClazz, @Nullable DeepClassScanner<?> contextClazz);

    public enum Group {
        SINGLETON,
        CONTEXTED
    }
}