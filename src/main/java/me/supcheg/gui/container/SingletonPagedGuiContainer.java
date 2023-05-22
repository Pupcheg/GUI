package me.supcheg.gui.container;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import me.supcheg.gui.util.GuiMap;
import me.supcheg.gui.annotation.Renderer;
import me.supcheg.gui.load.accessor.GuiAccessor;
import me.supcheg.gui.util.GuiUtil;
import me.supcheg.gui.render.PagedRenderer;
import me.supcheg.gui.render.TypedRenderer;
import me.supcheg.gui.tick.GuiAnimation;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SingletonPagedGuiContainer<I> extends SingletonGuiContainer<I> {
    private final Cache<Player, AtomicInteger> player2pageIndex;
    private final List<Inventory> inventories;
    private final List<Set<GuiAnimation>> animations;

    public SingletonPagedGuiContainer(int pageSize, @NotNull InventoryType inventoryType,
                                      @NotNull I guiInstance, @NotNull GuiAccessor<I> guiAccessor,
                                      boolean lockContents) {
        super(pageSize, inventoryType, guiInstance, guiAccessor, lockContents);
        this.player2pageIndex = CacheBuilder.newBuilder().weakKeys().expireAfterAccess(10, TimeUnit.MINUTES).build();
        this.inventories = new ArrayList<>();
        this.animations = new ArrayList<>();
        render();
    }

    @Unmodifiable
    @NotNull
    @Override
    protected Collection<TypedRenderer> newRenderers() {
        return Collections.singleton(new PagedRenderer(
                inventories, animations,
                pageIndex -> createInventory(guiAccessor.createTitle(guiInstance, pageIndex))
        ));
    }

    @SneakyThrows
    @Override
    public void open(@NotNull Player player) {
        AtomicInteger lastPage = player2pageIndex.get(player, AtomicInteger::new);
        GuiMap.put(player, this);
        player.openInventory(inventories.get(lastPage.get()));
    }

    public boolean openNextPage(@NotNull Player player) {
        AtomicInteger atomicInteger = player2pageIndex.getIfPresent(player);
        if (atomicInteger != null) {
            int value = atomicInteger.get();
            if (value + 1 >= inventories.size()) {
                return false;
            }
            atomicInteger.set(value + 1);
        }
        open(player);
        return true;
    }

    public boolean openPreviousPage(@NotNull Player player) {
        AtomicInteger atomicInteger = player2pageIndex.getIfPresent(player);
        if (atomicInteger != null) {
            int value = atomicInteger.get();
            if (value <= 0) {
                return false;
            }
            atomicInteger.set(value - 1);
        }
        open(player);
        return true;
    }

    @Override
    public void tick() {
        for (int pageIndex = 0; pageIndex < inventories.size(); pageIndex++) {
            Inventory inventory = inventories.get(pageIndex);
            if (!inventory.getViewers().isEmpty()) {
                acceptRenderers(guiAccessor.tick(guiInstance, getRenderers()));

                Set<GuiAnimation> pageAnimations = animations.get(pageIndex);
                for (GuiAnimation guiAnimation : pageAnimations) {
                    try {
                        guiAnimation.tick(inventory::setItem);
                    } catch (Exception ex) {
                        GuiUtil.logger().error("An error occurred while ticking {} to gui: {}", guiAnimation, guiInstance, ex);
                    }
                }

            }

        }
    }

    public void acceptRenderers(@NotNull Collection<TypedRenderer> renderers) {
        for (TypedRenderer renderer : renderers) {
            if (renderer.getType() == Renderer.PAGED) {
                PagedRenderer pagedRenderer = renderer.asPagedRenderer();

                for (int pageIndex = 0; pageIndex < pagedRenderer.getSize(); pageIndex++) {
                    pagedRenderer.page(pageIndex)
                            .asInventoryRenderer()
                            .renderContents(inventories.get(pageIndex)::setContents);
                }
            }
        }
    }

    @Override
    public void acceptInventoryEvent(@NotNull Player player, @NotNull InventoryEvent event) {
        int pageIndex = inventories.indexOf(event.getInventory());

        acceptRenderers(guiAccessor.acceptInventoryEvent(guiInstance, event, getRenderers(), pageIndex));
        lockContents(event);

        if (event instanceof InventoryCloseEvent) {
            GuiMap.remove(player, this);
        }
    }
}
