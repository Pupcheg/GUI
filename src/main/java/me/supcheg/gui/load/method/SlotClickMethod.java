package me.supcheg.gui.load.method;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.stream.IntStream;

public class SlotClickMethod extends PluginApplicableMethod {

    private final boolean handleDoubleClick;
    private final int[] slots;

    public SlotClickMethod(int[] slots, boolean handleDoubleClick, @NotNull Method method) {
        super(method);
        this.handleDoubleClick = handleDoubleClick;
        this.slots = slots;
    }

    public boolean shouldHandleDoubleClick() {
        return handleDoubleClick;
    }

    @NotNull
    public IntStream streamSlots() {
        return IntStream.of(slots);
    }
}
