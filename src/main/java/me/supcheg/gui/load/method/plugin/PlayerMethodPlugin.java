package me.supcheg.gui.load.method.plugin;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.reflection.ArgumentsResolver;

import java.lang.annotation.Annotation;
import java.util.Objects;

public abstract class PlayerMethodPlugin extends MethodPlugin {
    public PlayerMethodPlugin(@NotNull Annotation annotation) {
        super(annotation);
    }

    @Override
    public boolean run(@NotNull Object guiInstance, @NotNull ArgumentsResolver argumentsResolver) {
        Player player = Objects.requireNonNull(argumentsResolver.findWithType(Player.class), "Player not found");
        return run(player);
    }

    public abstract boolean run(@NotNull Player player);
}
