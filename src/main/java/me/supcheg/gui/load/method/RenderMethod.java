package me.supcheg.gui.load.method;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class RenderMethod extends PluginApplicableMethod {
    public RenderMethod(@NotNull Method method) {
        super(method);
    }
}
