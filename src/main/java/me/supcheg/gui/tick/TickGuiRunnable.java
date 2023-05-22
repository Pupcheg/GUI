package me.supcheg.gui.tick;

import me.supcheg.gui.container.GuiContainer;
import me.supcheg.gui.util.GuiUtil;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class TickGuiRunnable extends BukkitRunnable {

    private static final Collection<GuiContainer<?>> TICKING_CONTAINERS = new HashSet<>();
    private static boolean IS_SCHEDULED = false;

    public static void schedule() {
        if (!IS_SCHEDULED) {
            new TickGuiRunnable().runTaskTimer(GuiUtil.plugin(), 0, 1);
            IS_SCHEDULED = true;
        }
    }

    @NotNull
    @Contract("_ -> param1")
    public static <C extends GuiContainer<?>> C addTicking(@NotNull C container) {
        TICKING_CONTAINERS.add(container);
        return container;
    }

    public static boolean removeTicking(@NotNull GuiContainer<?> container) {
        return TICKING_CONTAINERS.remove(container);
    }

    @NotNull
    @UnmodifiableView
    public static Collection<GuiContainer<?>> getTicking() {
        return Collections.unmodifiableCollection(TICKING_CONTAINERS);
    }

    @Override
    public void run() {
        for (GuiContainer<?> container : TICKING_CONTAINERS) {
            container.tick();
        }
    }

}
