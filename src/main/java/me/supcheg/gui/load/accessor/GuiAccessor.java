package me.supcheg.gui.load.accessor;

import com.comphenix.protocol.events.PacketEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import me.supcheg.gui.container.GuiContainer;
import me.supcheg.gui.render.TypedRenderer;

import java.util.Collection;

public interface GuiAccessor<T> {

    @NotNull
    T newInstance(@NotNull GuiContainer<?> guiContainer, @NotNull Object @NotNull ... additionalArguments);

    void setContainerField(@NotNull T instance, @NotNull GuiContainer<?> guiContainer);

    @Nullable
    Component createTitle(@NotNull T instance, @NotNull Object @NotNull ... additionalArguments);

    @Unmodifiable
    @NotNull
    Collection<TypedRenderer> tick(@NotNull T instance, @NotNull Collection<TypedRenderer> renderers);

    @Unmodifiable
    @NotNull
    Collection<TypedRenderer> acceptPacketEvent(@NotNull T instance, @NotNull PacketEvent event, @NotNull Collection<TypedRenderer> renderers);

    @Unmodifiable
    @NotNull
    Collection<TypedRenderer> acceptInventoryEvent(@NotNull T instance, @NotNull InventoryEvent event, @NotNull Collection<TypedRenderer> renderers, @NotNull Object @NotNull ... additionalArguments);

    @Unmodifiable
    @NotNull
    Collection<TypedRenderer> appendRenderers(@NotNull T instance, @NotNull Collection<TypedRenderer> renderers);
}
