package me.supcheg.gui.load.accessor.loader;

import com.comphenix.protocol.PacketType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.SneakyThrows;
import me.supcheg.gui.annotation.AcceptDoubleClick;
import me.supcheg.gui.annotation.AutoWireContainer;
import me.supcheg.gui.annotation.Gui;
import me.supcheg.gui.annotation.InventoryEventHandler;
import me.supcheg.gui.annotation.OnTick;
import me.supcheg.gui.annotation.PacketHandler;
import me.supcheg.gui.annotation.Render;
import me.supcheg.gui.annotation.SlotClickHandler;
import me.supcheg.gui.annotation.Title;
import me.supcheg.gui.load.InvalidGuiClassStructureException;
import me.supcheg.gui.load.accessor.GuiAccessor;
import me.supcheg.gui.load.accessor.ReflectiveGuiAccessor;
import me.supcheg.gui.load.accessor.ReflectivePacketGuiAccessor;
import me.supcheg.gui.load.method.AcceptEventMethod;
import me.supcheg.gui.load.method.AcceptPacketEventMethod;
import me.supcheg.gui.load.method.GuiTickMethod;
import me.supcheg.gui.load.method.RenderMethod;
import me.supcheg.gui.load.method.SlotClickMethod;
import me.supcheg.gui.load.method.plugin.MethodPluginsRegistry;
import me.supcheg.gui.reflection.DeepClassScanner;
import me.supcheg.gui.tick.DynamicEventListener;
import me.supcheg.gui.util.IntRangeParser;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public abstract class AbstractReflectiveGuiAccessorLoader<C, B extends ReflectiveGuiAccessor.Builder<C, ReflectivePacketGuiAccessor<C>>> {

    private static final Pattern DOT_PATTERN = Pattern.compile(".", Pattern.LITERAL);

    protected final DeepClassScanner<C> classScanner;
    protected int inventorySize = -1;

    protected AbstractReflectiveGuiAccessorLoader(@NotNull DeepClassScanner<C> clazz) {
        this.classScanner = clazz;
    }

    public GuiAccessor<C> load() {
        B b = newBuilder();
        appendBuilder(b);
        return b.build();
    }

    protected abstract B newBuilder();

    protected void appendBuilder(@NotNull B builder) {
        builder
                .constructor(getConstructor())
                .containerFields(getContainerFields())
                .titleMethod(getTitleMethod())
                .packetType2handlers(getPacketTypeToHandlers())
                .eventClass2handlers(getEventClazzToHandlers())
                .slot2handlers(getSlotToHandlers())
                .tickMethods(getTickMethods())
                .appendRendererMethods(getAppendRendererMethods());
    }

    protected Constructor<C> getConstructor() {
        List<Constructor<C>> constructors = classScanner.getConstructors();
        if (constructors.size() != 1) {
            throw new InvalidGuiClassStructureException("Invalid constructors size: %d in %s", constructors.size(), classScanner);
        }

        return constructors.get(0);
    }

    protected Collection<Field> getContainerFields() {
        return List.copyOf(
                classScanner.getFieldsWith(AutoWireContainer.class)
                        .peek(field -> field.setAccessible(true))
                        .toList()
        );
    }

    protected Method getTitleMethod() {
        return classScanner.getMethodsWith(Title.class)
                .peek(AbstractReflectiveGuiAccessorLoader::checkIsPublic)
                .findFirst()
                .orElse(null);
    }

    @SneakyThrows
    protected Multimap<PacketType, AcceptPacketEventMethod> getPacketTypeToHandlers() {
        Multimap<PacketType, AcceptPacketEventMethod> packetTypeMethodMultimap = HashMultimap.create(12, 1);

        for (Method method : classScanner.getMethodsWith(PacketHandler.class).toList()) {
            checkIsPublic(method);
            String rawPacketType = method.getAnnotation(PacketHandler.class).value();

            String[] split = DOT_PATTERN.split(rawPacketType);

            Class<?> clazz = Class.forName(
                    PacketType.class.getName() +
                            (rawPacketType.substring("PacketType".length(), rawPacketType.lastIndexOf('.'))
                                    .replace('.', '$'))
            );

            String fieldName = split[split.length - 1];

            PacketType packetType = (PacketType) clazz.getField(fieldName).get(null);
            DynamicEventListener.registerIfAbsent(packetType);

            AcceptPacketEventMethod acceptPacketEventMethod = new AcceptPacketEventMethod(packetType, method);
            MethodPluginsRegistry.loadPlugins(acceptPacketEventMethod);

            packetTypeMethodMultimap.put(packetType, acceptPacketEventMethod);
        }

        return Multimaps.unmodifiableMultimap(packetTypeMethodMultimap);
    }

    protected Multimap<Class<? extends InventoryEvent>, AcceptEventMethod> getEventClazzToHandlers() {
        Multimap<Class<? extends InventoryEvent>, AcceptEventMethod> eventClazz2handlers = HashMultimap.create();

        for (Method method : classScanner.getMethodsWith(InventoryEventHandler.class).toList()) {
            checkIsPublic(method);
            Class<?>[] parametersTypes = method.getParameterTypes();

            if (parametersTypes.length == 0) {
                throw new InvalidGuiClassStructureException("Method annotated with %s must has at least 1 parameter: event",
                        InventoryEventHandler.class.getSimpleName());
            }

            Class<?> parameterType = parametersTypes[0];
            if (!InventoryEvent.class.isAssignableFrom(parameterType)) {
                throw new InvalidGuiClassStructureException("Invalid InventoryEvent type: %s", parameterType);
            }
            InventoryEventHandler eventHandler = method.getAnnotation(InventoryEventHandler.class);

            Class<? extends InventoryEvent> eventClazz = parameterType.asSubclass(InventoryEvent.class);
            DynamicEventListener.registerIfAbsent(eventClazz);

            AcceptEventMethod acceptEventMethod = new AcceptEventMethod(eventClazz, method, eventHandler.ignoreCancelled(), eventHandler.ignoreHandled());
            MethodPluginsRegistry.loadPlugins(acceptEventMethod);

            eventClazz2handlers.put(eventClazz, acceptEventMethod);

        }

        return Multimaps.unmodifiableMultimap(eventClazz2handlers);
    }

    @SuppressWarnings("PatternValidation")
    protected Int2ObjectMap<Collection<SlotClickMethod>> getSlotToHandlers() {
        Multimap<Integer, SlotClickMethod> slot2handlers = Multimaps.newMultimap(new Int2ObjectOpenHashMap<>(), HashSet::new);

        int fullSize = getFullSize();
        for (Method method : classScanner.getAllMethods()) {
            SlotClickHandler[] handlers = method.getAnnotationsByType(SlotClickHandler.class);
            if (handlers.length == 0) {
                continue;
            }

            checkIsPublic(method);

            IntStream slotsStream = IntStream.empty();

            for (SlotClickHandler handler : handlers) {
                IntStream range;

                int[] rawSlots = handler.value();
                String rawRange = handler.range();
                if (rawSlots.length != 0) {
                    range = IntStream.of(rawSlots);
                } else if (!rawRange.isEmpty()) {
                    range = IntRangeParser.parseIntRange(rawRange);
                } else {
                    range = IntStream.range(0, fullSize);
                }
                slotsStream = IntStream.concat(slotsStream, range);
            }

            int[] slots = slotsStream.sorted().toArray();
            boolean shouldHandleDoubleClick = method.isAnnotationPresent(AcceptDoubleClick.class);

            SlotClickMethod slotClickMethod = new SlotClickMethod(slots, shouldHandleDoubleClick, method);
            MethodPluginsRegistry.loadPlugins(slotClickMethod);

            for (int slot : slots) {
                slot2handlers.put(slot, slotClickMethod);
            }

        }

        return slot2handlers.isEmpty() ? Int2ObjectMaps.emptyMap() : new Int2ObjectOpenHashMap<>(slot2handlers.asMap());
    }

    protected Collection<GuiTickMethod> getTickMethods() {
        Collection<GuiTickMethod> tickMethods = new ArrayList<>();
        for (Method method : classScanner.getMethodsWith(OnTick.class).toList()) {
            checkIsPublic(method);

            int period = method.getAnnotation(OnTick.class).value();

            if (period <= 0) {
                throw new InvalidGuiClassStructureException("Period must be positive at %s, got: %d", method, period);
            }

            GuiTickMethod guiTickMethod = new GuiTickMethod(method, period);
            MethodPluginsRegistry.loadPlugins(guiTickMethod);

            tickMethods.add(guiTickMethod);
        }

        return List.copyOf(tickMethods);
    }

    protected Collection<RenderMethod> getAppendRendererMethods() {
        Collection<RenderMethod> renderMethods = new ArrayList<>();
        for (Method method : classScanner.getMethodsWith(Render.class).toList()) {
            checkIsPublic(method);

            RenderMethod renderMethod = new RenderMethod(method);
            MethodPluginsRegistry.loadPlugins(renderMethod);

            renderMethods.add(renderMethod);
        }
        return List.copyOf(renderMethods);
    }

    protected int getFullSize() {
        return getInventorySize();
    }

    protected int getInventorySize() {
        if (inventorySize == -1) {
            Gui gui = classScanner.findAnnotation(Gui.class);
            if (gui == null) {
                DeepClassScanner<?> enclosing = classScanner.getEnclosing();
                Objects.requireNonNull(enclosing);

                gui = enclosing.findAnnotation(Gui.class);
            }

            Objects.requireNonNull(gui);

            InventoryType inventoryType = gui.inventoryType();
            inventorySize = inventoryType == InventoryType.CHEST ? gui.size() : inventoryType.getDefaultSize();
        }

        return inventorySize;
    }

    protected static void checkIsPublic(@NotNull Member member) {
        if (!Modifier.isPublic(member.getModifiers())) {
            throw new InvalidGuiClassStructureException("%s must be public", member);
        }
    }
}
