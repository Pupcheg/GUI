package me.supcheg.gui.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import me.supcheg.gui.container.GuiContainer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EventListener;
import java.util.Map;
import java.util.WeakHashMap;

public class GuiHistory implements EventListener, Listener {

    private static final int MAX_HISTORY_CAPACITY = 50;

    private static final Map<Player, Deque<GuiContainer<?>>> PLAYER_OPEN_HISTORY = new WeakHashMap<>();

    public static void openPreviousOrClose(@NotNull Player player) {
        Deque<GuiContainer<?>> history = getHistory(player);
        if (history == null) {
            player.closeInventory();
            return;
        }

        history.pollLast();
        GuiContainer<?> previous = history.peekLast();
        if (previous != null) {
            previous.open(player);
        } else {
            player.closeInventory();
        }
    }

    public static void addLast(@NotNull Player player, @NotNull GuiContainer<?> container) {
        var history = PLAYER_OPEN_HISTORY.computeIfAbsent(player, id -> new ArrayDeque<>(MAX_HISTORY_CAPACITY));
        if (!container.equals(history.peekLast())) {

            if (history.size() >= MAX_HISTORY_CAPACITY) {
                history.pollFirst();
            }

            history.addLast(container);
        }
    }

    public static void clearHistory(@NotNull Player player) {
        PLAYER_OPEN_HISTORY.remove(player);
    }

    @Nullable
    public static Deque<GuiContainer<?>> getHistory(@NotNull Player player) {
        return PLAYER_OPEN_HISTORY.get(player);
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInventoryOpen(@NotNull InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        var guiContainer = GuiMap.get(player);

        if (guiContainer != null && guiContainer.shouldBeAddedToHistory()) {
            addLast(player, guiContainer);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        var history = getHistory(player);
        if (history == null) {
            return;
        }

        Bukkit.getScheduler().runTask(GuiUtil.plugin(), () -> {
            if (!player.isOnline() || player.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING) {
                clearHistory(player);
            }
        });
    }


}
