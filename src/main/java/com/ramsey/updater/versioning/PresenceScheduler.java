package com.ramsey.updater.versioning;

import com.ramsey.updater.Main;
import com.ramsey.updater.versioning.packets.PresencePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;
import java.util.concurrent.*;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PresenceScheduler {
    private static final ConcurrentHashMap<UUID, ScheduledFuture<?>> schedules = new ConcurrentHashMap<>();
    private static ScheduledExecutorService executorService;

    @SubscribeEvent
    public static void onServerStartUp(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        int maxPlayers = server.getMaxPlayers();

        executorService = Executors.newScheduledThreadPool(maxPlayers);
    }

    @SubscribeEvent
    public static void onServerShutdown(ServerStoppingEvent event) {
        if (executorService != null) {
            executorService.shutdown();
        }

        schedules.clear();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();

        ScheduledFuture<?> schedule = executorService.schedule(() -> {
            player.connection.disconnect(Component.literal("Your client failed to respond to the presence request"));
            schedules.remove(player.getUUID());
        }, 15, TimeUnit.SECONDS);

        schedules.put(player.getUUID(), schedule);

        NetworkHandler.sendToPlayer(player, new PresencePacket(PresencePacket.Mode.REQUEST));
    }

    public static void acknowledgePresence(ServerPlayer player) {
        Future<?> schedule = schedules.get(player.getUUID());

        if (schedule != null) {
            schedule.cancel(false);
            schedules.remove(player.getUUID());
        }
    }


}
