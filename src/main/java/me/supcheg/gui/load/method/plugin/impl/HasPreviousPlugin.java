package me.supcheg.gui.load.method.plugin.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.util.GuiHistory;
import me.supcheg.gui.container.GuiContainer;
import me.supcheg.gui.load.method.plugin.PlayerMethodPlugin;

import java.lang.annotation.Annotation;
import java.util.Deque;

public class HasPreviousPlugin extends PlayerMethodPlugin {
    public HasPreviousPlugin(@NotNull Annotation annotation) {
        super(annotation);
    }

    @Override
    public boolean run(@NotNull Player player) {
        Deque<GuiContainer<?>> history = GuiHistory.getHistory(player);
        return history != null && !history.isEmpty();
    }
}
