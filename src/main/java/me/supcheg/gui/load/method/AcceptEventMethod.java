package me.supcheg.gui.load.method;

import org.bukkit.event.inventory.InventoryEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class AcceptEventMethod extends PluginApplicableMethod {
    private final Class<? extends InventoryEvent> eventClazz;
    private final boolean ignoreCancelled;
    private final boolean ignoreHandled;

    public AcceptEventMethod(@NotNull Class<? extends InventoryEvent> eventClazz, @NotNull Method method, boolean ignoreCancelled, boolean ignoreHandled) {
        super(method);
        this.ignoreCancelled = ignoreCancelled;
        this.ignoreHandled = ignoreHandled;
        this.eventClazz = eventClazz;
    }

    @NotNull
    public Class<? extends InventoryEvent> getEventClass() {
        return eventClazz;
    }

    public boolean shouldHandle(boolean isHandled, boolean isCancelled) {
        return (!ignoreHandled || !isHandled) && (!ignoreCancelled || !isCancelled);
    }
}
