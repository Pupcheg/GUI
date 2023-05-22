package me.supcheg.gui.load.method;

import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.reflection.ArgumentsResolver;

import java.lang.reflect.Method;

public class GuiTickMethod extends PluginApplicableMethod {
    private final int period;
    private int ticksToNextRun;

    public GuiTickMethod(@NotNull Method method, int period) {
        super(method);
        this.period = period;
        this.ticksToNextRun = period;
    }

    @Override
    public boolean invokeWithPlugins(@NotNull Object guiInstance, @NotNull ArgumentsResolver resolver) {
        if (--ticksToNextRun <= 0) {
            ticksToNextRun = period;
            return super.invokeWithPlugins(guiInstance, resolver);
        }
        return false;
    }
}
