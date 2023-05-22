package me.supcheg.gui.load.method.plugin.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.container.SingletonPagedGuiContainer;
import me.supcheg.gui.load.method.plugin.MethodPlugin;
import me.supcheg.gui.reflection.ArgumentsResolver;

import java.lang.annotation.Annotation;
import java.util.Objects;

public class OpenNextPagePlugin extends MethodPlugin {
    public OpenNextPagePlugin(@NotNull Annotation annotation) {
        super(annotation);
    }

    @Override
    public boolean run(@NotNull Object guiInstance, @NotNull ArgumentsResolver argumentsResolver) {
        SingletonPagedGuiContainer<?> guiContainer = Objects.requireNonNull(
                argumentsResolver.findWithType(SingletonPagedGuiContainer.class),
                "SingletonPagedGuiContainer not found"
        );
        Player player = Objects.requireNonNull(argumentsResolver.findWithType(Player.class), "Player not found");

        return guiContainer.openNextPage(player);
    }
}
