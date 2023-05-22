package me.supcheg.gui.load.method.plugin.impl;

import me.supcheg.gui.annotation.PlaySound;
import me.supcheg.gui.load.method.plugin.PlayerMethodPlugin;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("PatternValidation")
public class PlaySoundPlugin extends PlayerMethodPlugin {

    private final Sound sound;

    public PlaySoundPlugin(@NotNull PlaySound annotation) {
        super(annotation);

        String rawSound = annotation.value().trim();

        Sound.Source source = annotation.source();
        float volume = annotation.volume();
        float pitch = annotation.pitch();

        sound = Sound.sound(Key.key(rawSound), source, volume, pitch);
    }

    @Override
    public boolean run(@NotNull Player player) {
        player.playSound(sound);
        return true;
    }
}
