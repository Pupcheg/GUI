package me.supcheg.gui.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import me.supcheg.gui.container.GuiContainer;

import java.util.HashMap;
import java.util.Map;

public class GuiMap {
    private static final Map<Player, GuiContainer<?>> PLAYER_TO_VIEWING_CONTAINER = new HashMap<>();
    private static final Map<Player, GuiContainer<?>> PLAYER_TO_NEXT_CONTAINER = new HashMap<>();

    private GuiMap() {
        throw new UnsupportedOperationException();
    }

    public static void put(@NotNull Player player, @NotNull GuiContainer<?> guiContainer) {
        if (PLAYER_TO_VIEWING_CONTAINER.containsKey(player)) {
            PLAYER_TO_NEXT_CONTAINER.put(player, guiContainer);
        } else {
            PLAYER_TO_VIEWING_CONTAINER.put(player, guiContainer);
        }
    }

    public static void remove(@NotNull Player player, @NotNull GuiContainer<?> guiContainer) {
        PLAYER_TO_VIEWING_CONTAINER.remove(player, guiContainer);

        GuiContainer<?> nextContainer = PLAYER_TO_NEXT_CONTAINER.remove(player);
        if (nextContainer != null) {
            PLAYER_TO_VIEWING_CONTAINER.put(player, nextContainer);
        }
    }

    @Nullable
    public static GuiContainer<?> get(@NotNull Player player) {
        return PLAYER_TO_VIEWING_CONTAINER.get(player);
    }

}
