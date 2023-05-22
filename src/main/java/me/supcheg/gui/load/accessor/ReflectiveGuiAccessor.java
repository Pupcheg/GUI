package me.supcheg.gui.load.accessor;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.AdventureComponentConverter;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.Getter;
import me.supcheg.gui.load.method.AcceptEventMethod;
import me.supcheg.gui.load.method.AcceptPacketEventMethod;
import me.supcheg.gui.load.method.GuiTickMethod;
import me.supcheg.gui.load.method.RenderMethod;
import me.supcheg.gui.load.method.SlotClickMethod;
import me.supcheg.gui.reflection.ArgumentsResolver;
import me.supcheg.gui.render.TypedRenderer;
import me.supcheg.gui.util.GuiUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.event.Cancellable;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReflectiveGuiAccessor<T> extends ReflectiveSimpleGuiAccessor<T> {

    protected final Map<T, ArgumentsResolver> instance2resolver;

    protected final Method titleMethod;
    protected final Multimap<PacketType, AcceptPacketEventMethod> packetType2handlers;
    protected final Multimap<Class<? extends InventoryEvent>, AcceptEventMethod> eventClass2handlers;
    protected final Int2ObjectMap<Collection<SlotClickMethod>> slot2handlers;
    protected final Collection<GuiTickMethod> tickMethods;
    protected final Collection<RenderMethod> renderMethods;

    @Contract(pure = true)
    protected ReflectiveGuiAccessor(@NotNull Builder<T, ? extends ReflectiveGuiAccessor<T>> builder) {
        super(Objects.requireNonNull(builder.constructor), Objects.requireNonNull(builder.containerFields));
        this.titleMethod = builder.titleMethod;
        this.packetType2handlers = Objects.requireNonNull(builder.packetType2handlers);
        this.eventClass2handlers = Objects.requireNonNull(builder.eventClass2handlers);
        this.slot2handlers = Objects.requireNonNull(builder.slot2handlers);
        this.tickMethods = Objects.requireNonNull(builder.tickMethods);
        this.renderMethods = Objects.requireNonNull(builder.appendRendererMethods);
        this.instance2resolver = new HashMap<>();
    }

    @Override
    protected void onInstanceCreate(@NotNull T instance, @NotNull ArgumentsResolver argumentsResolver) {
        instance2resolver.put(instance, argumentsResolver);
    }

    @Override
    @Nullable
    public Component createTitle(@NotNull T instance, @NotNull Object @NotNull ... additionalArguments) {
        if (titleMethod == null) {
            return null;
        }
        try {
            Object rawTitle = instance2resolver.get(instance).newChild().append(additionalArguments).invoke(instance, titleMethod);
            if (rawTitle instanceof ComponentLike componentLike) {
                return componentLike.asComponent();
            } else if (rawTitle instanceof WrappedChatComponent wrappedChatComponent) {
                return AdventureComponentConverter.fromWrapper(wrappedChatComponent);
            } else {
                return Component.text(String.valueOf(rawTitle));
            }
        } catch (Exception ex) {
            GuiUtil.logger().error("An error occurred while getting title from gui: {}", instance, ex);
            return null;
        }
    }

    @Override
    @Unmodifiable
    @NotNull
    public Collection<TypedRenderer> tick(@NotNull T instance, @NotNull Collection<TypedRenderer> renderers) {
        Collection<TypedRenderer> rendered;
        if (!tickMethods.isEmpty()) {

            ArgumentsResolver currentResolver = instance2resolver.get(instance).newChild();
            rendered = appendResolverWithRenderers(currentResolver, renderers);

            for (GuiTickMethod tickMethod : tickMethods) {
                try {
                    tickMethod.invokeWithPlugins(instance, currentResolver);
                } catch (Exception ex) {
                    GuiUtil.logger().error("An error occurred while ticking {} to gui: {}", instance.getClass(), instance, ex);
                }
            }
        } else {
            rendered = Collections.emptySet();
        }
        return rendered;
    }

    @NotNull
    @Override
    public Collection<TypedRenderer> acceptPacketEvent(@NotNull T instance, @NotNull PacketEvent event, @NotNull Collection<TypedRenderer> renderers) {
        if (packetType2handlers.isEmpty()) {
            return Collections.emptySet();
        }

        var handlers = packetType2handlers.get(event.getPacketType());
        if (handlers.isEmpty()) {
            return Collections.emptySet();
        }

        ArgumentsResolver currentResolver = instance2resolver.get(instance).newChild().append(event, event.getPacket(), event.getPlayer());
        Collection<TypedRenderer> rendered = appendResolverWithRenderers(currentResolver, renderers);
        for (AcceptPacketEventMethod handler : handlers) {
            try {
                handler.invokeWithPlugins(instance, currentResolver);
            } catch (Exception ex) {
                GuiUtil.logger().error("An error occurred while performing PacketEvent with type {} to gui: {}", event.getPacketType(), instance, ex);
            }
        }
        return rendered;
    }

    @Unmodifiable
    @NotNull
    @Override
    public Collection<TypedRenderer> acceptInventoryEvent(@NotNull T instance, @NotNull InventoryEvent event,
                                                          @NotNull Collection<TypedRenderer> renderers,
                                                          @NotNull Object @NotNull ... additionalArguments) {
        ArgumentsResolver currentResolver = instance2resolver.get(instance).newChild()
                .append(additionalArguments)
                .append(event, event.getView().getPlayer());
        var rendered = appendResolverWithRenderers(currentResolver, renderers);

        boolean isHandled;
        if (event instanceof InventoryClickEvent clickEvent) {
            if (clickEvent.getClick() == ClickType.DOUBLE_CLICK) {
                acceptSlotClick(instance, clickEvent, currentResolver, true);
                return rendered;
            } else {
                isHandled = acceptSlotClick(instance, clickEvent, currentResolver, false);
            }
        } else {
            isHandled = false;
        }

        if (eventClass2handlers.isEmpty()) {
            return rendered;
        }

        Class<? extends InventoryEvent> actualEventClazz = event.getClass();
        var handlers = eventClass2handlers.get(actualEventClazz);
        if (handlers.isEmpty()) {
            return rendered;
        }

        boolean isCancelled = event instanceof Cancellable c && c.isCancelled();
        for (AcceptEventMethod method : handlers) {
            try {
                if (method.shouldHandle(isHandled, isCancelled)) {
                    method.invokeWithPlugins(instance, currentResolver);
                }
            } catch (Exception ex) {
                GuiUtil.logger().error("An error occurred while performing {} to gui: {}", actualEventClazz.getSimpleName(), instance, ex);
            }
        }

        return rendered;
    }

    protected boolean acceptSlotClick(@NotNull T instance, @NotNull InventoryClickEvent clickEvent, @NotNull ArgumentsResolver currentResolver, boolean onlyDoubleClick) {
        boolean handled = false;
        if (!slot2handlers.isEmpty()) {
            Inventory clickedInventory = clickEvent.getClickedInventory();

            if (clickedInventory == null || clickedInventory.getType() != InventoryType.PLAYER) {
                int slot = clickEvent.getSlot();
                var currentSlotMethods = slot2handlers.get(slot);
                if (currentSlotMethods != null) {
                    for (var currentSlotMethod : currentSlotMethods) {
                        if (onlyDoubleClick && !currentSlotMethod.shouldHandleDoubleClick()) {
                            continue;
                        }
                        try {
                            handled |= currentSlotMethod.invokeWithPlugins(instance, currentResolver);
                        } catch (Exception ex) {
                            GuiUtil.logger().error("An error occurred while accepting click to a gui: {} at slot: {}",
                                    instance, slot, ex);
                        }
                    }
                }
            }

        }

        return handled;
    }

    @Override
    @Unmodifiable
    @NotNull
    public Collection<TypedRenderer> appendRenderers(@NotNull T instance, @NotNull Collection<TypedRenderer> renderers) {
        if (!renderMethods.isEmpty()) {
            ArgumentsResolver currentResolver = instance2resolver.get(instance).newChild();

            var rendered = appendResolverWithRenderers(currentResolver, renderers);
            for (RenderMethod renderMethod : renderMethods) {
                try {
                    renderMethod.invokeWithPlugins(instance, currentResolver);
                } catch (Exception ex) {
                    GuiUtil.logger().error("An error occurred while rendering a gui: {}", instance, ex);
                }
            }

            return rendered;
        }

        return Collections.emptySet();
    }

    @Unmodifiable
    @NotNull
    private Collection<TypedRenderer> appendResolverWithRenderers(@NotNull ArgumentsResolver resolver,
                                                                  @NotNull Collection<TypedRenderer> renderers) {
        List<TypedRenderer> rendered = new ArrayList<>(renderers.size());

        for (TypedRenderer renderer : renderers) {
            resolver.appendLazy(
                    TypedRenderer.class,
                    () -> {
                        rendered.add(renderer);
                        return renderer;
                    },
                    renderer
            );
        }
        return Collections.unmodifiableCollection(rendered);
    }


    @Getter
    public static class Builder<T, A extends ReflectiveGuiAccessor<T>> {
        private Constructor<T> constructor;
        private Collection<Field> containerFields;
        private Method titleMethod;
        private Multimap<PacketType, AcceptPacketEventMethod> packetType2handlers;
        private Multimap<Class<? extends InventoryEvent>, AcceptEventMethod> eventClass2handlers;
        private Int2ObjectMap<Collection<SlotClickMethod>> slot2handlers;
        private Collection<GuiTickMethod> tickMethods;
        private Collection<RenderMethod> appendRendererMethods;

        public Builder() {
        }

        public Builder<T, A> constructor(Constructor<T> constructor) {
            this.constructor = constructor;
            return this;
        }

        public Builder<T, A> containerFields(Collection<Field> containerFields) {
            this.containerFields = containerFields;
            return this;
        }

        public Builder<T, A> titleMethod(Method titleMethod) {
            this.titleMethod = titleMethod;
            return this;
        }

        public Builder<T, A> packetType2handlers(Multimap<PacketType, AcceptPacketEventMethod> packetType2handlers) {
            this.packetType2handlers = packetType2handlers;
            return this;
        }

        public Builder<T, A> eventClass2handlers(Multimap<Class<? extends InventoryEvent>, AcceptEventMethod> eventClass2handlers) {
            this.eventClass2handlers = eventClass2handlers;
            return this;
        }

        public Builder<T, A> slot2handlers(Int2ObjectMap<Collection<SlotClickMethod>> slot2handlers) {
            this.slot2handlers = slot2handlers;
            return this;
        }

        public Builder<T, A> tickMethods(Collection<GuiTickMethod> tickMethods) {
            this.tickMethods = tickMethods;
            return this;
        }

        public Builder<T, A> appendRendererMethods(Collection<RenderMethod> appendRendererMethod) {
            this.appendRendererMethods = appendRendererMethod;
            return this;
        }

        public ReflectiveGuiAccessor<T> build() {
            return new ReflectiveGuiAccessor<>(this);
        }
    }
}
