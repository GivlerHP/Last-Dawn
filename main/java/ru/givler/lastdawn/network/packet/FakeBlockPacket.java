package ru.givler.lastdawn.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import ru.givler.lastdawn.block.SanityBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FakeBlockPacket {

    public enum Mode { HIDE, SHOW }

    private final List<BlockPos> positions;
    private final Mode mode;

    public FakeBlockPacket(List<BlockPos> positions, Mode mode) {
        this.positions = positions;
        this.mode = mode;
    }

    public static void encode(FakeBlockPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.mode);
        buf.writeInt(packet.positions.size());
        for (BlockPos pos : packet.positions) {
            buf.writeBlockPos(pos);
        }
    }

    public static FakeBlockPacket decode(FriendlyByteBuf buf) {
        Mode mode = buf.readEnum(Mode.class);
        int count = buf.readInt();
        List<BlockPos> positions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            positions.add(buf.readBlockPos());
        }
        return new FakeBlockPacket(positions, mode);
    }

    public static void handle(FakeBlockPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var mc = Minecraft.getInstance();
            System.out.println("FakeBlockPacket received! mode=" + packet.mode + " count=" + packet.positions.size());
            if (mc.level == null) {
                System.out.println("mc.level is NULL!");
                return;
            }

            for (BlockPos pos : packet.positions) {
                if (packet.mode == Mode.HIDE) {
                    BlockState state = mc.level.getBlockState(pos);
                    if (state.getBlock() instanceof SanityBlock) {
                        mc.level.setBlock(pos, state.setValue(SanityBlock.GHOST, true), 0);
                    }
                } else {
                    mc.levelRenderer.setSectionDirty(
                            pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4
                    );
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}