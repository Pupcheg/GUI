package me.supcheg.gui.load.method.plugin;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.annotation.*;
import me.supcheg.gui.load.method.PluginApplicableMethod;
import me.supcheg.gui.load.method.plugin.impl.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MethodPluginsRegistry {
    private static final Map<Class<? extends Annotation>, Function<Annotation, MethodPlugin>> TYPE_TO_PLUGIN = Maps.newHashMapWithExpectedSize(9);

    static {
        putPlugin(PlaySound.class, PlaySoundPlugin::new);
        putPlugin(ExecuteCommand.class, ExecuteCommandPlugin::new);
        putPlugin(CloseGui.class, CloseGuiPlugin::new);
        putPlugin(OpenPreviousGuiOrClose.class, OpenPreviousOrClosePlugin::new);
        putPlugin(OpenNextPage.class, OpenNextPagePlugin::new);
        putPlugin(OpenPreviousPage.class, OpenPreviousPagePlugin::new);
        putPlugin(HasPermission.class, HasPermissionPlugin::new);
        putPlugin(Limit.class, LimitedExecutionsPlugin::new);
        putPlugin(HasPreviousGui.class, HasPreviousPlugin::new);
    }

    @SuppressWarnings("unchecked")
    public static <A extends Annotation> void putPlugin(@NotNull Class<A> clazz, @NotNull Function<A, MethodPlugin> constructor) {
        TYPE_TO_PLUGIN.put(clazz, (Function<Annotation, MethodPlugin>) constructor);
    }

    public static void loadPlugins(@NotNull PluginApplicableMethod pluginApplicableMethod) {
        Method method = pluginApplicableMethod.getMethod();

        ArrayList<MethodPlugin> pluginsBefore = new ArrayList<>(2);
        ArrayList<MethodPlugin> pluginsAfter = new ArrayList<>(2);

        TYPE_TO_PLUGIN.forEach((clazz, constructor) -> {
            if (method.isAnnotationPresent(clazz)) {

                Annotation annotation = method.getAnnotation(clazz);
                if (annotation != null) {
                    MethodPlugin plugin = constructor.apply(annotation);
                    if (plugin != null) {

                        List<MethodPlugin> plugins = switch (plugin.getPointCut()) {
                            case BEFORE -> pluginsBefore;
                            case AFTER -> pluginsAfter;
                        };
                        plugins.add(plugin);
                    }
                }
            }
        });

        if (!pluginsBefore.isEmpty()) {
            pluginsBefore.trimToSize();
            pluginsBefore.sort(Comparator.naturalOrder());
            pluginApplicableMethod.setPluginsBefore(pluginsBefore);
        }

        if (!pluginsAfter.isEmpty()) {
            pluginsAfter.trimToSize();
            pluginsAfter.sort(Comparator.naturalOrder());
            pluginApplicableMethod.setPluginsAfter(pluginsAfter);
        }
    }

}
