package me.supcheg.gui.load.method;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import me.supcheg.gui.load.method.plugin.MethodPlugin;
import me.supcheg.gui.reflection.ArgumentsResolver;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public abstract class PluginApplicableMethod {
    protected final Method method;
    protected Collection<MethodPlugin> pluginsBefore;
    protected Collection<MethodPlugin> pluginsAfter;

    public PluginApplicableMethod(@NotNull Method method) {
        this.method = method;
        this.pluginsBefore = Collections.emptySet();
        this.pluginsAfter = Collections.emptySet();
    }

    @NotNull
    public Method getMethod() {
        return method;
    }

    public void setPluginsBefore(@NotNull Collection<MethodPlugin> pluginsBefore) {
        this.pluginsBefore = Objects.requireNonNull(pluginsBefore, "pluginsBefore");
    }

    public void setPluginsAfter(@NotNull Collection<MethodPlugin> pluginsAfter) {
        this.pluginsAfter = Objects.requireNonNull(pluginsAfter, "pluginsAfter");
    }

    public boolean invokeWithPlugins(@NotNull Object guiInstance, @NotNull ArgumentsResolver resolver) {
        boolean result;

        for (MethodPlugin plugin : pluginsBefore) {
            result = plugin.run(guiInstance, resolver);
            if (!result) {
                return false;
            }
        }

        result = asBoolean(resolver.invoke(guiInstance, method));

        if (!result) {
            return false;
        }

        for (MethodPlugin plugin : pluginsAfter) {
            result = plugin.run(guiInstance, resolver);
            if (!result) {
                return false;
            }
        }
        return true;
    }

    protected boolean asBoolean(@Nullable Object o) {
        return !(o instanceof Boolean bool) || bool;
    }
}
