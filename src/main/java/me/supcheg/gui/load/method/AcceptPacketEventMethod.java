package me.supcheg.gui.load.method;

import com.comphenix.protocol.PacketType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class AcceptPacketEventMethod extends PluginApplicableMethod {
    private final PacketType packetType;

    public AcceptPacketEventMethod(@NotNull PacketType packetType, @NotNull Method method) {
        super(method);
        this.packetType = packetType;
    }

    @NotNull
    public PacketType getPacketType() {
        return packetType;
    }
}
