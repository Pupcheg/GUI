package me.supcheg.gui.load.type;

import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import me.supcheg.gui.container.GuiContainer;
import me.supcheg.gui.load.accessor.GuiAccessor;
import me.supcheg.gui.reflection.DeepClassScanner;

public abstract class ContextedType extends GuiType {
    protected ContextedType() {
        super(Group.CONTEXTED);
    }

    @NotNull
    @Override
    public final <I> GuiContainer<I> newSingletonContainer(int size, @NotNull InventoryType inventoryType,
                                                           @NotNull I instance, @NotNull GuiAccessor<I> guiAccessor,
                                                           boolean lockContents) {
        throw new UnsupportedOperationException("Is not SINGLETON: " + this);
    }

    @Override
    public boolean test(@NotNull DeepClassScanner<?> mainClazz, @Nullable DeepClassScanner<?> contextClazz) {
        return contextClazz != null && test(contextClazz);
    }

    public abstract boolean test(@NotNull DeepClassScanner<?> contextClazz);
}
