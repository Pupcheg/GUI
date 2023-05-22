package me.supcheg.gui.load.type;

import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import me.supcheg.gui.container.GuiContainer;
import me.supcheg.gui.load.accessor.GuiAccessor;
import me.supcheg.gui.reflection.DeepClassScanner;

public abstract class SingletonType extends GuiType {
    protected SingletonType() {
        super(Group.SINGLETON);
    }

    @Override
    public final boolean test(@NotNull DeepClassScanner<?> mainClazz, @Nullable DeepClassScanner<?> contextClazz) {
        return contextClazz == null && test(mainClazz);
    }

    public abstract boolean test(@NotNull DeepClassScanner<?> mainClazz);

    @NotNull
    @Override
    public final <I> GuiContainer<I> newContextedContainer(int size, @NotNull InventoryType inventoryType,
                                                           I instance, @NotNull GuiAccessor<I> guiAccessor,
                                                           @NotNull GuiAccessor<?> contextAccessor,
                                                           boolean lockContents) {
        throw new UnsupportedOperationException("Is not CONTEXTED: " + this);
    }
}
