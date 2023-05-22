package me.supcheg.gui.load;

import me.supcheg.gui.reflection.DeepClassScanner;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import me.supcheg.gui.annotation.Gui;
import me.supcheg.gui.container.GuiContainer;
import me.supcheg.gui.load.accessor.loader.GuiAccessorLoaders;
import me.supcheg.gui.load.type.*;

import java.util.List;
import java.util.Objects;

public class GuiLoader {
    private static final List<GuiType> TYPES = List.of(
            new ContextedCurrentInventoryType(),
            new ContextedPacketType(),
            new ContextedStandardType(),

            new SingletonPagedType(),
            new SingletonStandardType()
    );

    private final DeepClassScanner<?> clazz;
    private final DeepClassScanner<?> contextClazz;

    private final InventoryType inventoryType;
    private final boolean lockContents;
    private final int size;
    private final GuiType guiType;

    private GuiLoader(@NotNull DeepClassScanner<?> classScanner) {
        this.clazz = classScanner;
        this.contextClazz = this.clazz.getFirstSealed();
        this.guiType = getType(clazz, contextClazz);

        Gui guiAnnotation = Objects.requireNonNull(classScanner.findAnnotation(Gui.class),
                "Cannot find @Gui in " + classScanner.getJavaClass().getName());

        this.inventoryType = guiAnnotation.inventoryType();
        this.lockContents = guiAnnotation.lockContents();
        this.size = guiAnnotation.size();
    }

    @NotNull
    @Contract("_ -> new")
    public static GuiLoader of(@NotNull Class<?> clazz) {
        return new GuiLoader(DeepClassScanner.of(clazz));
    }

    @NotNull
    @Contract("_ -> new")
    public static <T, C extends GuiContainer<T>> C loadInstance(@NotNull T guiInstance) {
        return GuiLoader.of(guiInstance.getClass()).load(guiInstance);
    }

    @NotNull
    public static GuiType getType(@NotNull DeepClassScanner<?> mainClazz, @Nullable DeepClassScanner<?> contextClazz) {
        for (GuiType type : TYPES) {
            if (type.test(mainClazz, contextClazz)) {
                return type;
            }
        }
        throw new IllegalArgumentException(mainClazz.toString());
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <I, C extends GuiContainer<I>> C load(@NotNull I instance) {
        DeepClassScanner<I> clazzScanner = (DeepClassScanner<I>) clazz;

        return (C) switch (guiType.getGroup()) {
            case SINGLETON -> guiType.newSingletonContainer(
                    size, inventoryType,
                    instance, guiType.loadGuiAccessor(clazzScanner),
                    lockContents
            );
            case CONTEXTED -> guiType.newContextedContainer(
                    size, inventoryType,
                    instance, GuiAccessorLoaders.loadSimpleReflective(clazzScanner),
                    guiType.loadGuiAccessor(contextClazz),
                    lockContents
            );
        };
    }
}
