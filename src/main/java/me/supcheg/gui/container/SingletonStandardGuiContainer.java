package me.supcheg.gui.container;

import me.supcheg.gui.util.GuiMap;
import me.supcheg.gui.annotation.Renderer;
import me.supcheg.gui.load.accessor.GuiAccessor;
import me.supcheg.gui.util.GuiUtil;
import me.supcheg.gui.render.InventoryRenderer;
import me.supcheg.gui.render.TypedRenderer;
import me.supcheg.gui.tick.GuiAnimation;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SingletonStandardGuiContainer<I> extends SingletonGuiContainer<I> {
    protected final Inventory inventory;
    protected final Set<GuiAnimation> animations;

    public SingletonStandardGuiContainer(int size, @NotNull InventoryType inventoryType,
                                         @NotNull I instance, @NotNull GuiAccessor<I> guiAccessor,
                                         boolean lockContents) {
        super(size, inventoryType, instance, guiAccessor, lockContents);
        this.animations = new HashSet<>();
        this.inventory = createInventory(guiAccessor.createTitle(guiInstance));
        render();
    }

    @Unmodifiable
    @NotNull
    @Override
    protected Collection<TypedRenderer> newRenderers() {
        return Collections.singleton(new InventoryRenderer(Renderer.INVENTORY, inventory.getContents(), animations));
    }

    @Override
    public void open(@NotNull Player player) {
        GuiMap.put(player, this);
        player.openInventory(inventory);
    }

    @Override
    public void tick() {
        if (!inventory.getViewers().isEmpty()) {
            acceptRenderers(guiAccessor.tick(guiInstance, getRenderers()));
            for (GuiAnimation guiAnimation : animations) {
                try {
                    guiAnimation.tick(inventory::setItem);
                } catch (Exception ex) {
                    GuiUtil.logger().error("An error occurred while ticking {} to gui: {}", guiAnimation, guiInstance, ex);
                }
            }
        }
    }

    public void acceptRenderers(@NotNull Collection<TypedRenderer> renderers) {
        for (TypedRenderer renderer : renderers) {
            if (renderer.getType() == Renderer.INVENTORY) {
                renderer.asInventoryRenderer().renderContents(inventory::setContents);
            }
        }
    }
}
