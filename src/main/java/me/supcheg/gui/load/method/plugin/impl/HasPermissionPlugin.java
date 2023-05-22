package me.supcheg.gui.load.method.plugin.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.annotation.HasPermission;
import me.supcheg.gui.load.method.plugin.PlayerMethodPlugin;

public class HasPermissionPlugin extends PlayerMethodPlugin {

    private final String permission;

    public HasPermissionPlugin(@NotNull HasPermission annotation) {
        super(annotation);

        this.permission = annotation.value();
    }

    @Override
    public boolean run(@NotNull Player player) {
        return player.hasPermission(permission);
    }
}
