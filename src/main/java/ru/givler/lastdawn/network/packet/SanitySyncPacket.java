package ru.givler.lastdawn.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import ru.givler.lastdawn.sanity.SanityProvider;

import java.util.function.Supplier;

public class SanitySyncPacket {

    private final int sanity;

    public SanitySyncPacket(int sanity) {
        this.sanity = sanity;
    }

    //servet to client
    public static void encode(ru.givler.lastdawn.network.packet.SanitySyncPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.sanity);
    }

    public static ru.givler.lastdawn.network.packet.SanitySyncPacket decode(FriendlyByteBuf buf) {
        return new ru.givler.lastdawn.network.packet.SanitySyncPacket(buf.readInt());
    }

    public static void handle(ru.givler.lastdawn.network.packet.SanitySyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player == null) return;

            player.getCapability(SanityProvider.SANITY_CAP)
                    .ifPresent(sanity -> sanity.setSanity(packet.sanity));
        });
        ctx.get().setPacketHandled(true);
    }
}