package ru.givler.lastdawn.events;


import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import ru.givler.lastdawn.block.SanityBlock;
import ru.givler.lastdawn.network.packet.FakeBlockPacket;
import ru.givler.lastdawn.network.LDNetwork;
import ru.givler.lastdawn.sanity.ISanity;
import ru.givler.lastdawn.sanity.SanityStage;
import net.minecraftforge.network.NetworkDirection;

import java.util.ArrayList;
import java.util.List;

public class SanityBlockTracker {

    private static final int SCAN_RADIUS = 128;

    public static void onStageChanged(ServerPlayer player, ISanity sanity,
                                      SanityStage oldStage, SanityStage newStage) {
        Level level = player.level();

        BlockPos center = player.blockPosition();
        List<BlockPos> toHide = new ArrayList<>();
        List<BlockPos> toShow = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                center.offset(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS))) {

            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof SanityBlock sanityBlock)) continue;

            boolean wasGhost = sanityBlock.isGhostStage(oldStage);
            boolean isGhost  = sanityBlock.isGhostStage(newStage);

            if (!wasGhost && isGhost) {
                toHide.add(pos.immutable());
            } else if (wasGhost && !isGhost) {
                toShow.add(pos.immutable());
            }
        }

        if (!toHide.isEmpty()) {
            System.out.println("Sending FakeBlockPacket HIDE to " + player.getName().getString() + " count=" + toHide.size());
            LDNetwork.CHANNEL.sendTo(
                    new FakeBlockPacket(toHide, FakeBlockPacket.Mode.HIDE),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
        }

        if (!toShow.isEmpty()) {
            System.out.println("Sending FakeBlockPacket SHOW to " + player.getName().getString() + " count=" + toShow.size());
            for (BlockPos pos : toShow) {
                player.connection.send(new ClientboundBlockUpdatePacket(level, pos));
            }
            pushPlayerOutOfBlocks(player, level, toShow);
        }
    }

    private static void pushPlayerOutOfBlocks(ServerPlayer player, Level level,
                                              List<BlockPos> solidifiedBlocks) {
        AABB playerBox = player.getBoundingBox();

        for (BlockPos pos : solidifiedBlocks) {
            AABB blockBox = new AABB(pos);
            if (!playerBox.intersects(blockBox)) continue;

            double pushX = 0, pushY = 0, pushZ = 0;
            double minDist = Double.MAX_VALUE;

            double[] distances = {
                    pos.getX() + 1 - playerBox.minX, // +X
                    playerBox.maxX - pos.getX(),      // -X
                    pos.getY() + 1 - playerBox.minY,  // +Y (вверх — приоритет)
                    playerBox.maxY - pos.getY(),       // -Y
                    pos.getZ() + 1 - playerBox.minZ,  // +Z
                    playerBox.maxZ - pos.getZ()        // -Z
            };

            double upDist = distances[2];
            if (upDist < 1.5) {
                pushY = upDist + 0.1;
            } else {

                int[] dirs = {0, 1, 3, 4, 5};
                for (int i : dirs) {
                    if (distances[i] < minDist) {
                        minDist = distances[i];
                        pushX = (i == 0) ? distances[0] + 0.1 : (i == 1) ? -(distances[1] + 0.1) : 0;
                        pushY = 0;
                        pushZ = (i == 4) ? distances[4] + 0.1 : (i == 5) ? -(distances[5] + 0.1) : 0;
                    }
                }
            }

            player.teleportTo(
                    player.getX() + pushX,
                    player.getY() + pushY,
                    player.getZ() + pushZ
            );
            break;
        }
    }
}