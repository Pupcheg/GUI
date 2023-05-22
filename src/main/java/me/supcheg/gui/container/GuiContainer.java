package me.supcheg.gui.container;

import com.comphenix.protocol.events.PacketEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import me.supcheg.gui.load.accessor.GuiAccessor;
import me.supcheg.gui.tick.TickGuiRunnable;

public abstract class GuiContainer<I> {
    protected final I guiInstance;
    protected final int size;
    protected final GuiAccessor<I> guiAccessor;
    protected final InventoryType inventoryType;
    protected final boolean lockContents;

    protected GuiContainer(int size, @NotNull InventoryType inventoryType,
                           @NotNull I guiInstance, @NotNull GuiAccessor<I> guiAccessor,
                           boolean lockContents) {
        this.guiInstance = guiInstance;
        guiAccessor.setContainerField(guiInstance, this);
        this.size = size;
        this.guiAccessor = guiAccessor;
        this.inventoryType = inventoryType;
        this.lockContents = lockContents;

        TickGuiRunnable.addTicking(this);
    }

    @NotNull
    public I getGuiInstance() {
        return guiInstance;
    }

    public abstract void open(@NotNull Player player);

    public abstract void tick();

    public abstract void acceptInventoryEvent(@NotNull Player player, @NotNull InventoryEvent event);

    public abstract void acceptPacketEvent(@NotNull PacketEvent event);

    public boolean shouldBeAddedToHistory() {
        return true;
    }

    protected void lockContents(@NotNull InventoryEvent event) {
        if (lockContents && event instanceof InventoryInteractEvent interactEvent) {
            interactEvent.setCancelled(true);
        }
    }

    @NotNull
    @Contract("_ -> new")
    protected Inventory createInventory(@Nullable Component title) {
        Inventory inventory;
        if (inventoryType == InventoryType.CHEST) {
            if (title != null) {
                inventory = Bukkit.createInventory(null, size, title);
            } else {
                inventory = Bukkit.createInventory(null, size);
            }
        } else {
            if (title != null) {
                inventory = Bukkit.createInventory(null, inventoryType, title);
            } else {
                inventory = Bukkit.createInventory(null, inventoryType);
            }
        }
        return inventory;
    }

}
