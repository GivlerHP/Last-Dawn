package ru.givler.lastdawn.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import ru.givler.lastdawn.mechanics.FragileManager;

import java.util.function.Supplier;

public class FragileSyncPacket {

    private final BlockPos pos;
    private final int stage;

    public FragileSyncPacket(BlockPos pos, int stage) {
        this.pos = pos;
        this.stage = stage;
    }

    public static void encode(FragileSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeInt(packet.stage);
    }

    public static FragileSyncPacket decode(FriendlyByteBuf buf) {
        return new FragileSyncPacket(buf.readBlockPos(), buf.readInt());
    }

    public static void handle(FragileSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            FragileManager.setClientStage(packet.pos, packet.stage);
        });
        ctx.get().setPacketHandled(true);
    }
}