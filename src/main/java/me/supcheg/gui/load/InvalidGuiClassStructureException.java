package me.supcheg.gui.load;

import org.jetbrains.annotations.NotNull;

public class InvalidGuiClassStructureException extends RuntimeException {
    public InvalidGuiClassStructureException(@NotNull String message, Object... format) {
        super(message.formatted(format));
    }
}
