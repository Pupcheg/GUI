package me.supcheg.gui.tick;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class GuiAnimation {
    private static final NextIndexBiFunction RANDOM_ITEM_STACK = (index, size) -> {
        int currentIndex;
        do {
            currentIndex = ThreadLocalRandom.current().nextInt(size);
        } while (currentIndex == index);
        return currentIndex;
    };
    private static final NextIndexBiFunction SEQUENTIAL_ITEM_STACK = (index, size) -> ++index >= size ? 0 : index;

    protected final int periodInTicks;
    protected final List<ItemStack> itemStacks;
    protected final int slot;
    protected final NextIndexBiFunction nextIndexFunction;

    protected int ticksToNextChange;
    protected int index;

    public GuiAnimation(int periodInTicks, @NotNull List<ItemStack> itemStacks, int slot,
                        @NotNull NextIndexBiFunction nextIndexBiFunction) {
        this.periodInTicks = periodInTicks;
        this.itemStacks = itemStacks;
        this.slot = slot;
        this.nextIndexFunction = nextIndexBiFunction;
        this.ticksToNextChange = 0;
        this.index = -1;
    }

    @NotNull
    public static GuiAnimation newAnimation(int periodInTicks, @NotNull List<ItemStack> itemStacks, boolean random, int slot) {
        return new GuiAnimation(periodInTicks, itemStacks, slot, random ? RANDOM_ITEM_STACK : SEQUENTIAL_ITEM_STACK);
    }

    public int getSlot() {
        return slot;
    }

    public int getPeriodInTicks() {
        return periodInTicks;
    }

    @NotNull
    @UnmodifiableView
    public List<ItemStack> getItemStacks() {
        return Collections.unmodifiableList(itemStacks);
    }

    public boolean tick(@NotNull AnimationBiConsumer consumer) {
        if (--ticksToNextChange <= 0) {
            ticksToNextChange = periodInTicks;
            index = nextIndexFunction.apply(index, itemStacks.size());
            consumer.accept(slot, itemStacks.get(index));
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof GuiAnimation that)) {
            return false;
        }

        return slot == that.slot
                && periodInTicks == that.periodInTicks
                && itemStacks.equals(that.itemStacks)
                && nextIndexFunction == that.nextIndexFunction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                itemStacks,
                slot,
                periodInTicks,
                nextIndexFunction
        );
    }

    @FunctionalInterface
    public interface NextIndexBiFunction {
        int apply(int index, int size);
    }

    @FunctionalInterface
    public interface AnimationBiConsumer {
        void accept(int slot, @Nullable ItemStack itemStack);
    }
}
