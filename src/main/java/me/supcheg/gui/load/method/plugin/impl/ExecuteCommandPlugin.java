package me.supcheg.gui.load.method.plugin.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.annotation.ExecuteCommand;
import me.supcheg.gui.load.method.plugin.PlayerMethodPlugin;

public class ExecuteCommandPlugin extends PlayerMethodPlugin {

    private final String command;

    public ExecuteCommandPlugin(@NotNull ExecuteCommand annotation) {
        super(annotation);
        this.command = annotation.value();
    }

    @Override
    public boolean run(@NotNull Player player) {
        return Bukkit.dispatchCommand(player, command);
    }
}
