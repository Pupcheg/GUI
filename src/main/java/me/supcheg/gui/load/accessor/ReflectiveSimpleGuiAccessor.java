package me.supcheg.gui.load.accessor;

import com.comphenix.protocol.events.PacketEvent;
import me.supcheg.gui.container.GuiContainer;
import me.supcheg.gui.util.GuiUtil;
import me.supcheg.gui.reflection.ArgumentsResolver;
import me.supcheg.gui.render.TypedRenderer;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;

public class ReflectiveSimpleGuiAccessor<T> implements GuiAccessor<T> {

    protected final Constructor<T> constructor;
    protected final Collection<Field> containerFields;

    public ReflectiveSimpleGuiAccessor(@NotNull Constructor<T> constructor,
                                       @Nullable Collection<Field> containerField) {
        this.constructor = constructor;
        this.containerFields = containerField;
    }

    @NotNull
    @Override
    public T newInstance(@NotNull GuiContainer<?> guiContainer, @NotNull Object @NotNull ... additionalArguments) {
        ArgumentsResolver currentResolver = new ArgumentsResolver().append(additionalArguments).append(guiContainer);

        T instance = currentResolver.newInstance(constructor);
        setContainerField0(instance, guiContainer);
        onInstanceCreate(instance, currentResolver);

        return instance;
    }

    @Override
    public void setContainerField(@NotNull T instance, @NotNull GuiContainer<?> guiContainer) {
        setContainerField0(instance, guiContainer);
        onInstanceCreate(instance, new ArgumentsResolver().append(guiContainer));
    }

    public void setContainerField0(@NotNull T instance, @NotNull GuiContainer<?> guiContainer) {
        if (containerFields != null) {
            for (Field containerField : containerFields) {
                try {
                    containerField.set(instance, guiContainer);
                } catch (Exception ex) {
                    GuiUtil.logger().error("An error occurred while wiring container to {} in gui: {}", containerField, instance, ex);
                }
            }

        }
    }

    protected void onInstanceCreate(@NotNull T instance, @NotNull ArgumentsResolver argumentsResolver) {
        // Поддержка ReflectiveGuiAccessor
    }

    @Nullable
    @Override
    public Component createTitle(@NotNull T instance, @NotNull Object @NotNull ... additionalArguments) {
        throw new UnsupportedOperationException();
    }

    @Override
    @NotNull
    @Unmodifiable
    public Collection<TypedRenderer> tick(@NotNull T instance, @NotNull Collection<TypedRenderer> renderers) {
        throw new UnsupportedOperationException();
    }

    @Override
    @NotNull
    @Unmodifiable
    public Collection<TypedRenderer> acceptPacketEvent(@NotNull T instance, @NotNull PacketEvent event, @NotNull Collection<TypedRenderer> renderers) {
        throw new UnsupportedOperationException();
    }

    @Override
    @NotNull
    @Unmodifiable
    public Collection<TypedRenderer> acceptInventoryEvent(@NotNull T instance, @NotNull InventoryEvent event, @NotNull Collection<TypedRenderer> renderers, @NotNull Object @NotNull ... additionalArguments) {
        throw new UnsupportedOperationException();
    }

    @Override
    @NotNull
    @Unmodifiable
    public Collection<TypedRenderer> appendRenderers(@NotNull T instance, @NotNull Collection<TypedRenderer> renderers) {
        throw new UnsupportedOperationException();
    }
}
