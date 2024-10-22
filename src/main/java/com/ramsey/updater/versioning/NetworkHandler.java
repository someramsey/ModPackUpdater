package com.ramsey.updater.versioning;

import com.ramsey.updater.Main;
import com.ramsey.updater.versioning.packets.PresencePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
        .named(Main.resource("main_channel"))
        .serverAcceptedVersions((version) -> true)
        .clientAcceptedVersions((version) -> true)
        .networkProtocolVersion(() -> "1")
        .simpleChannel();

    public static void register() {
        INSTANCE.messageBuilder(PresencePacket.class, 0)
            .encoder(PresencePacket::encode)
            .decoder(PresencePacket::new)
            .consumerMainThread(PresencePacket::handle).add();
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }
}
