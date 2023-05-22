package me.supcheg.gui.util;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.PluginClassLoader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public final class GuiUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger("GUI");
    private static final Plugin PLUGIN = Objects.requireNonNull(((PluginClassLoader) GuiUtil.class.getClassLoader()).getPlugin());

    private GuiUtil() {
    }

    @NotNull
    public static Logger logger() {
        return LOGGER;
    }

    @NotNull
    public static Plugin plugin() {
        return PLUGIN;
    }
}
