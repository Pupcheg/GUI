package me.supcheg.gui.render;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.supcheg.gui.annotation.Renderer;
import me.supcheg.gui.annotation.RendererType;
import me.supcheg.gui.tick.GuiAnimation;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class InventoryRenderer implements TypedRenderer {
    protected final Renderer type;
    protected final ItemStack[] contents;
    protected final Set<GuiAnimation> animations;

    public InventoryRenderer(@NotNull Renderer type, @Nullable ItemStack @NotNull [] contents,
                             @NotNull Set<GuiAnimation> animations) {
        this.type = type;
        this.contents = contents;
        this.animations = animations;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Unmodifiable
    protected static Set<GuiAnimation> @NotNull [] createAnimationsMatrix(int size, @NotNull Iterator<GuiAnimation> it) {
        Int2ObjectMap<Set<GuiAnimation>> slot2animations = new Int2ObjectArrayMap<>(size / 2);
        while (it.hasNext()) {
            GuiAnimation animation = it.next();
            int slot = animation.getSlot();
            slot2animations.computeIfAbsent(slot, s -> new HashSet<>())
                    .add(animation);
        }

        Set<GuiAnimation>[] matrix = new Set[size];
        for (int slot = 0; slot < matrix.length; slot++) {
            var set = slot2animations.get(slot);
            if (set == null) {
                set = Collections.emptySet();
            } else {
                set = Collections.unmodifiableSet(set);
            }
            matrix[slot] = set;
        }

        return matrix;
    }

    public void clear() {
        Arrays.fill(contents, null);
        animations.clear();
    }

    public void clear(int startIndex, int endIndex) {
        if (endIndex < startIndex) {
            throw new IllegalArgumentException("Start: " + startIndex + ", End: " + endIndex);
        }

        clear(contents, animations, startIndex, endIndex);
    }

    protected static void clear(ItemStack[] contents, @NotNull Set<GuiAnimation> animations, int startIndex, int endIndex) {
        Arrays.fill(contents, startIndex, endIndex, null);

        var it = animations.iterator();
        while (it.hasNext()) {
            int animationSlot = it.next().getSlot();
            if (animationSlot >= startIndex && animationSlot < endIndex) {
                it.remove();
            }
        }
    }

    public void renderContents(@NotNull Consumer<@Nullable ItemStack @NotNull []> consumer) {
        consumer.accept(contents);
    }

    @Override
    @NotNull
    public Renderer getType() {
        return type;
    }

    @Override
    public boolean test(@NotNull Parameter parameter) {
        return type == Renderer.INVENTORY ?
                (!parameter.isAnnotationPresent(RendererType.class) ||
                        parameter.getAnnotation(RendererType.class).value() == Renderer.INVENTORY) :
                (parameter.isAnnotationPresent(RendererType.class) &&
                        parameter.getAnnotation(RendererType.class).value() == type);
    }

    public int getSize() {
        return contents.length;
    }

    @Nullable
    public ItemStack @NotNull [] getContents() {
        return contents.clone();
    }

    @NotNull
    @UnmodifiableView
    public Set<GuiAnimation> getAnimations() {
        return Collections.unmodifiableSet(animations);
    }

    @NotNull
    @Unmodifiable
    public Set<GuiAnimation> @NotNull [] getAnimationsMatrix() {
        return createAnimationsMatrix(contents.length, animations.iterator());
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public SlotRenderer iconRange(@NotNull IntStream range) {
        return itemStack -> range.forEach(i -> contents[i] = clone(itemStack));
    }

    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public SlotRenderer iconRange(int fromSlot, int toSlot) {
        return itemStack -> {
            for (int i = fromSlot; i < toSlot; i++) {
                contents[i] = clone(itemStack);
            }
        };
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public SlotRenderer icon(int slot) {
        return itemStack -> contents[slot] = itemStack;
    }


    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public AnimationRenderer animationRange(@NotNull IntStream range) {
        return new AnimationRenderer() {
            @Override
            void render(int period, @NotNull List<ItemStack> itemStacks, boolean random) {
                if (itemStacks.size() == 1) {
                    iconRange(range).render(itemStacks.get(0));
                    return;
                }
                range.forEach(slot -> addAnimation(period, itemStacks, random, slot));
            }
        };
    }

    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public AnimationRenderer animationRange(int fromSlot, int toSlot) {
        return new AnimationRenderer() {
            @Override
            void render(int period, @NotNull List<ItemStack> itemStacks, boolean random) {
                if (itemStacks.size() == 1) {
                    iconRange(fromSlot, toSlot).render(itemStacks.get(0));
                    return;
                }

                for (int slot = fromSlot; slot < toSlot; slot++) {
                    addAnimation(period, itemStacks, random, slot);
                }
            }
        };
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public AnimationRenderer animation(int slot) {
        return new AnimationRenderer() {
            @Override
            void render(int period, @NotNull List<ItemStack> itemStacks, boolean random) {
                if (itemStacks.size() == 1) {
                    icon(slot).render(itemStacks.get(0));
                    return;
                }
                addAnimation(period, itemStacks, random, slot);
            }
        };
    }

    protected void addAnimation(int periodInTicks, @NotNull List<ItemStack> itemStacks, boolean random, int slot) {
        GuiAnimation animation = GuiAnimation.newAnimation(periodInTicks, itemStacks, random, slot);
        animations.add(animation);
        animation.tick((i, itemStack) -> contents[i] = itemStack);
    }

    @Override
    public String toString() {
        return "InventoryRenderer{"
                + "type=" + type
                + '}';
    }

    public interface SlotRenderer {
        void render(@Nullable ItemStack itemStack);

        default void render(@Nullable Material material) {
            render(material == null ? null : new ItemStack(material));
        }
    }

    public abstract static class AnimationRenderer {
        private final List<ItemStack> itemStacks;
        private boolean random;
        private int period;

        protected AnimationRenderer() {
            this.period = -1;
            this.itemStacks = new ArrayList<>();
        }

        @NotNull
        @Contract(value = "_ -> this", pure = true)
        public AnimationRenderer random(boolean random) {
            this.random = random;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", pure = true)
        public AnimationRenderer period(@Range(from = 1, to = Integer.MAX_VALUE) int period) {
            this.period = period;
            return this;
        }

        @NotNull
        @Contract(value = "_, _ -> this", pure = true)
        public AnimationRenderer period(@Range(from = 1, to = Long.MAX_VALUE) long period, @NotNull TimeUnit timeUnit) {
            this.period = (int) (timeUnit.toMillis(period) / 50D);
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", pure = true)
        public AnimationRenderer append(@NotNull Iterable<ItemStack> itemStacks) {
            for (ItemStack itemStack : itemStacks) {
                this.itemStacks.add(itemStack);
            }
            return this;
        }

        @NotNull
        @Contract(value = "_, _ -> this", pure = true)
        public AnimationRenderer append(@NotNull ItemStack first, @NotNull ItemStack @NotNull ... itemStacks) {
            this.itemStacks.add(first);
            this.itemStacks.addAll(Arrays.asList(itemStacks));
            return this;
        }

        public void renderAnimation() {
            if (period == -1) {
                throw new IllegalStateException("Period must be set");
            }

            if (itemStacks.isEmpty()) {
                return;
            }

            render(period, itemStacks, random);
        }

        abstract void render(int period, @NotNull List<ItemStack> itemStacks, boolean random);
    }

    @Nullable
    @Contract("!null -> param1; null -> null")
    protected static ItemStack clone(@Nullable ItemStack itemStack) {
        return itemStack == null ? null : itemStack.clone();
    }

}
