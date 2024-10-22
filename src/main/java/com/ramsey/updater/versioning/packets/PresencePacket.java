package com.ramsey.updater.versioning.packets;

import com.ramsey.updater.versioning.NetworkHandler;
import com.ramsey.updater.versioning.PresenceScheduler;
import io.netty.buffer.ByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PresencePacket {
    private final Mode mode;

    public PresencePacket(Mode mode) {
        this.mode = mode;
    }

    public PresencePacket(ByteBuf buf) {
        this.mode = Mode.values()[buf.readByte()];
    }

    public void encode(ByteBuf buf) {
        buf.writeByte(this.mode.ordinal());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        switch (this.mode) {
            case REQUEST:
                NetworkHandler.sendToServer(new PresencePacket(Mode.ACKNOWLEDGE));
                break;
            case ACKNOWLEDGE:
                ServerPlayer player = context.getSender();

                if (player == null) {
                    return;
                }

                PresenceScheduler.acknowledgePresence(player);
                break;
        }

        context.setPacketHandled(true);
    }

    public enum Mode {
        REQUEST,
        ACKNOWLEDGE
    }
}
