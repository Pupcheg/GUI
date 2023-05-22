package me.supcheg.gui.render;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.annotation.Renderer;
import me.supcheg.gui.annotation.RendererType;
import me.supcheg.gui.tick.GuiAnimation;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;

public class PagedRenderer implements TypedRenderer {

    private final List<InventoryRenderer> renderers;
    private final List<Inventory> inventories;
    private final List<Set<GuiAnimation>> animations;
    private final IntFunction<Inventory> inventoryFunction;

    public PagedRenderer(@NotNull List<Inventory> inventories, @NotNull List<Set<GuiAnimation>> animations, @NotNull IntFunction<Inventory> inventoryFunction) {
        this.renderers = new ArrayList<>();
        this.inventories = inventories;
        this.animations = animations;
        this.inventoryFunction = inventoryFunction;
    }

    public int getSize() {
        return inventories.size();
    }

    @NotNull
    public InventoryRenderer nextPage() {
        Inventory newInventory = inventoryFunction.apply(renderers.size());

        Set<GuiAnimation> newAnimations = new HashSet<>();
        InventoryRenderer renderer = new InventoryRenderer(Renderer.INVENTORY, newInventory.getContents(), newAnimations);

        inventories.add(newInventory);
        animations.add(newAnimations);
        renderers.add(renderer);

        return renderer;
    }

    @NotNull
    public InventoryRenderer page(int pageIndex) {
        return renderers.get(pageIndex);
    }

    @NotNull
    @Override
    public Renderer getType() {
        return Renderer.PAGED;
    }

    @Override
    public boolean test(@NotNull Parameter parameter) {
        return !parameter.isAnnotationPresent(RendererType.class) ||
                parameter.getAnnotation(RendererType.class).value() == Renderer.PAGED;
    }
}
