package me.supcheg.gui.load.method.plugin.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.load.method.plugin.PlayerMethodPlugin;

import java.lang.annotation.Annotation;

public class CloseGuiPlugin extends PlayerMethodPlugin {
    public CloseGuiPlugin(@NotNull Annotation annotation) {
        super(annotation);
    }

    @Override
    public boolean run(@NotNull Player player) {
        player.closeInventory();
        return true;
    }
}
