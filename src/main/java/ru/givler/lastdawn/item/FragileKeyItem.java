package ru.givler.lastdawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import ru.givler.lastdawn.mechanics.FragileManager;

public class FragileKeyItem extends Item {

    public FragileKeyItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!LockingKeyItem.isLockable(state)) {
            return InteractionResult.PASS;
        }

        BlockPos normalizedPos = LockingKeyItem.getNormalizedPos(level, pos, state);

        Player player = ctx.getPlayer();
        if (!level.isClientSide()) {
            if (FragileManager.isFragile(normalizedPos, level)) {
                if (player != null) player.sendSystemMessage(
                        Component.literal("§eУже хрупкий!")
                );
            } else {
                FragileManager.setFragile(normalizedPos, level);
                sendFragilePacket(level, normalizedPos, 1);
                if (player != null) player.sendSystemMessage(
                        Component.literal("§6Стал хрупким!")
                );
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void sendFragilePacket(Level level, BlockPos pos, int stage) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            ru.givler.lastdawn.network.NetworkLD.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.NEAR.with(() ->
                            new net.minecraftforge.network.PacketDistributor.TargetPoint(
                                    pos.getX(), pos.getY(), pos.getZ(), 64, serverLevel.dimension()
                            )
                    ),
                    new ru.givler.lastdawn.network.packet.FragileSyncPacket(pos, stage)
            );
        }
    }
}