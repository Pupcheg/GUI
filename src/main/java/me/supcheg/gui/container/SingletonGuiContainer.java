package me.supcheg.gui.container;

import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import me.supcheg.gui.util.GuiMap;
import me.supcheg.gui.load.accessor.GuiAccessor;
import me.supcheg.gui.render.TypedRenderer;

import java.util.Collection;

public abstract class SingletonGuiContainer<I> extends GuiContainer<I> {
    protected Collection<TypedRenderer> renderers;

    protected SingletonGuiContainer(int size, @NotNull InventoryType inventoryType,
                                    @NotNull I guiInstance, @NotNull GuiAccessor<I> guiAccessor,
                                    boolean lockContents) {
        super(size, inventoryType, guiInstance, guiAccessor, lockContents);
    }

    public void render() {
        acceptRenderers(guiAccessor.appendRenderers(guiInstance, getRenderers()));
    }

    @Unmodifiable
    @NotNull
    protected Collection<TypedRenderer> getRenderers() {
        if (renderers == null) {
            renderers = newRenderers();
        }
        return renderers;
    }

    @Unmodifiable
    @NotNull
    protected abstract Collection<TypedRenderer> newRenderers();

    public abstract void acceptRenderers(@NotNull Collection<TypedRenderer> renderers);

    @Override
    public void acceptInventoryEvent(@NotNull Player player, @NotNull InventoryEvent event) {
        acceptRenderers(guiAccessor.acceptInventoryEvent(guiInstance, event, getRenderers()));
        lockContents(event);

        if (event instanceof InventoryCloseEvent) {
            GuiMap.remove(player, this);
        }
    }

    @Override
    public void acceptPacketEvent(@NotNull PacketEvent event) {
        acceptRenderers(guiAccessor.acceptPacketEvent(guiInstance, event, getRenderers()));
    }

}
