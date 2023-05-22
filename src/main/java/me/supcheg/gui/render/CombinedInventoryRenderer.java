package me.supcheg.gui.render;

import com.google.common.collect.Iterators;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import me.supcheg.gui.annotation.Renderer;
import me.supcheg.gui.tick.GuiAnimation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class CombinedInventoryRenderer extends InventoryRenderer {

    private final ItemStack[] additionalContents;
    private final Set<GuiAnimation> additionalAnimations;

    public CombinedInventoryRenderer(@Nullable ItemStack @NotNull [] firstContents,
                                     @Nullable ItemStack @NotNull [] secondContents,
                                     @NotNull Set<GuiAnimation> firstAnimations,
                                     @NotNull Set<GuiAnimation> secondAnimations) {
        super(Renderer.COMBINED, firstContents, firstAnimations);
        this.additionalContents = secondContents;
        this.additionalAnimations = secondAnimations;
    }

    @Override
    public void clear() {
        super.clear();
        Arrays.fill(additionalContents, null);
        additionalAnimations.clear();
    }

    @Override
    public void clear(int startIndex, int endIndex) {
        if (endIndex < startIndex) {
            throw new IllegalArgumentException("Start: " + startIndex + ", End: " + endIndex);
        }

        if (endIndex <= contents.length) {
            clear(contents, animations, startIndex, endIndex);
            return;
        }

        if (startIndex >= contents.length) {
            startIndex -= contents.length;
            endIndex -= contents.length;

            clear(additionalContents, additionalAnimations, startIndex, endIndex);
            return;
        }

        clear(contents, animations, startIndex, contents.length);
        clear(additionalContents, additionalAnimations, 0, endIndex);
    }

    @Override
    public int getSize() {
        return contents.length + additionalContents.length;
    }

    @Override
    @Nullable
    public ItemStack @NotNull [] getContents() {
        ItemStack[] clone = Arrays.copyOf(contents, getSize());
        System.arraycopy(additionalContents, 0, clone, clone.length - additionalContents.length, additionalContents.length);
        return clone;
    }

    @Override
    @NotNull
    @Unmodifiable
    public Set<GuiAnimation> getAnimations() {
        Set<GuiAnimation> newSet = new HashSet<>(animations.size() + additionalAnimations.size());
        newSet.addAll(animations);
        newSet.addAll(additionalAnimations);
        return Collections.unmodifiableSet(newSet);
    }

    @Override
    @NotNull
    @Unmodifiable
    public Set<GuiAnimation> @NotNull [] getAnimationsMatrix() {
        return createAnimationsMatrix(getSize(), Iterators.concat(animations.iterator(), additionalAnimations.iterator()));
    }

    @Override
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public SlotRenderer iconRange(@NotNull IntStream range) {
        return itemStack -> range.forEach(slot -> contentsForSlot(slot)[convertSlot(slot)] = clone(itemStack));
    }

    @Override
    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public SlotRenderer iconRange(int fromSlot, int toSlot) {
        return itemStack -> {
            for (int slot = fromSlot; slot < toSlot; slot++) {
                contentsForSlot(slot)[convertSlot(slot)] = clone(itemStack);
            }
        };
    }

    @Override
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public SlotRenderer icon(int slot) {
        return itemStack -> contentsForSlot(slot)[convertSlot(slot)] = itemStack;
    }

    @Override
    protected void addAnimation(int periodInTicks, @NotNull List<ItemStack> itemStacks, boolean random, int slot) {
        GuiAnimation animation = GuiAnimation.newAnimation(periodInTicks, itemStacks, random, convertSlot(slot));
        animationsForSlot(slot).add(animation);
        animation.tick((s, itemStack) -> contentsForSlot(s)[s] = itemStack);
    }

    @Nullable
    private ItemStack @NotNull [] contentsForSlot(int slot) {
        return slot >= contents.length ? additionalContents : contents;
    }

    @NotNull
    private Set<GuiAnimation> animationsForSlot(int slot) {
        return slot >= contents.length ? additionalAnimations : animations;
    }

    private int convertSlot(int slot) {
        if (slot >= contents.length) {
            slot -= contents.length;
        }
        return slot;
    }

}
