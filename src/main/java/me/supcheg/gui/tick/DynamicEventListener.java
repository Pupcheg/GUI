package me.supcheg.gui.tick;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.supcheg.gui.util.GuiUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;
import me.supcheg.gui.util.GuiMap;
import me.supcheg.gui.container.GuiContainer;

import java.util.HashSet;
import java.util.Set;

public class DynamicEventListener {
    private static final Set<PacketType> REGISTERED_PACKET_TYPES = new HashSet<>();
    private static final Set<Class<? extends InventoryEvent>> REGISTERED_BUKKIT_EVENTS = new HashSet<>();
    private static final Listener LISTENER = new Listener() {
    };
    private static final EventExecutor EXECUTOR = (l, e) -> {
        InventoryEvent event = (InventoryEvent) e;
        Player player = (Player) event.getView().getPlayer();

        GuiContainer<?> guiContainer = GuiMap.get(player);

        if (guiContainer != null) {
            guiContainer.acceptInventoryEvent(player, event);
        }
    };

    public static void registerIfAbsent(@NotNull PacketType packetType) {
        if (REGISTERED_PACKET_TYPES.add(packetType)) {

            PacketAdapter packetListener = switch (packetType.getSender()) {
                case CLIENT ->
                        new PacketAdapter(PacketAdapter.params(GuiUtil.plugin(), packetType).clientSide()) {
                            @Override
                            public void onPacketReceiving(@NotNull PacketEvent event) {
                                onPacketEvent(event);
                            }
                        };
                case SERVER ->
                        new PacketAdapter(PacketAdapter.params(GuiUtil.plugin(), packetType).serverSide()) {
                            @Override
                            public void onPacketSending(@NotNull PacketEvent event) {
                                onPacketEvent(event);
                            }
                        };
            };

            ProtocolLibrary.getProtocolManager().addPacketListener(packetListener);
        }
    }

    private static void onPacketEvent(@NotNull PacketEvent event) {
        GuiContainer<?> guiContainer = GuiMap.get(event.getPlayer());
        if (guiContainer != null) {
            guiContainer.acceptPacketEvent(event);
        }
    }

    public static void registerIfAbsent(@NotNull Class<? extends InventoryEvent> eventClazz) {
        if (REGISTERED_BUKKIT_EVENTS.add(eventClazz)) {
            Bukkit.getPluginManager().registerEvent(eventClazz, LISTENER, EventPriority.NORMAL, EXECUTOR, GuiUtil.plugin(), false);
        }
    }

    public static void setup() {
        registerIfAbsent(InventoryClickEvent.class);
        registerIfAbsent(InventoryDragEvent.class);
        registerIfAbsent(InventoryCloseEvent.class);

        registerIfAbsent(PacketType.Play.Server.WINDOW_ITEMS);
        registerIfAbsent(PacketType.Play.Server.SET_SLOT);
        registerIfAbsent(PacketType.Play.Server.OPEN_WINDOW);
    }
}
