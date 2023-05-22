package me.supcheg.gui.render;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.annotation.Renderer;

import java.lang.reflect.Parameter;
import java.util.function.Predicate;

public interface TypedRenderer extends Predicate<Parameter> {
    @NotNull
    Renderer getType();

    boolean test(@NotNull Parameter parameter);

    @NotNull
    @Contract("-> this")
    default InventoryRenderer asInventoryRenderer() {
        if (this instanceof InventoryRenderer inventoryRenderer) {
            return inventoryRenderer;
        }
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Contract("-> this")
    default PagedRenderer asPagedRenderer() {
        if (this instanceof PagedRenderer pagedRenderer) {
            return pagedRenderer;
        }
        throw new UnsupportedOperationException();
    }
}
