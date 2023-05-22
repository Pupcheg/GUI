package me.supcheg.gui.load.method.plugin.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.util.GuiHistory;
import me.supcheg.gui.load.method.plugin.PlayerMethodPlugin;

import java.lang.annotation.Annotation;

public class OpenPreviousOrClosePlugin extends PlayerMethodPlugin {
    public OpenPreviousOrClosePlugin(@NotNull Annotation annotation) {
        super(annotation);
    }

    @Override
    public boolean run(@NotNull Player player) {
        GuiHistory.openPreviousOrClose(player);
        return true;
    }
}
