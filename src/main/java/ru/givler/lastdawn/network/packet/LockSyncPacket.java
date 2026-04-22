package ru.givler.lastdawn.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import ru.givler.lastdawn.mechanics.LockManager;

import java.util.function.Supplier;

public class LockSyncPacket {

    private final BlockPos pos;
    private final boolean locked;

    public LockSyncPacket(BlockPos pos, boolean locked) {
        this.pos = pos;
        this.locked = locked;
    }

    public static void encode(LockSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeBoolean(packet.locked);
    }

    public static LockSyncPacket decode(FriendlyByteBuf buf) {
        return new LockSyncPacket(buf.readBlockPos(), buf.readBoolean());
    }

    public static void handle(LockSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LockManager.setClientLocked(packet.pos, packet.locked);
        });
        ctx.get().setPacketHandled(true);
    }
}