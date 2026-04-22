package ru.givler.lastdawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.network.PacketDistributor;
import ru.givler.lastdawn.mechanics.LockManager;
import ru.givler.lastdawn.network.NetworkLD;
import ru.givler.lastdawn.network.packet.LockSyncPacket;

public class LockingKeyItem extends Item {

    public LockingKeyItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!isLockable(state)) {
            return InteractionResult.PASS;
        }

        BlockPos normalizedPos = getNormalizedPos(level, pos, state);

        Player player = ctx.getPlayer();
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            if (LockManager.isLocked(normalizedPos, level)) {
                LockManager.unlock(normalizedPos, level);
                sendPackets(serverLevel, normalizedPos, false);
                if (player != null) player.sendSystemMessage(Component.literal("§aЗамок снят!"));
            } else {
                LockManager.lock(normalizedPos, level);
                sendPackets(serverLevel, normalizedPos, true);
                if (player != null) player.sendSystemMessage(Component.literal("§cЗаблокировано!"));
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static BlockPos getNormalizedPos(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof DoorBlock) {
            DoubleBlockHalf half = state.getValue(DoorBlock.HALF);
            return half == DoubleBlockHalf.UPPER ? pos.below() : pos;
        }
        return pos;
    }

    public static boolean isLockable(BlockState state) {
        return state.getBlock() instanceof TrapDoorBlock
                || state.getBlock() instanceof DoorBlock;
    }

    private static void sendPackets(ServerLevel serverLevel, BlockPos pos, boolean locked) {
        PacketDistributor.TargetPoint point = new PacketDistributor.TargetPoint(
                pos.getX(), pos.getY(), pos.getZ(), 64, serverLevel.dimension()
        );
        NetworkLD.CHANNEL.send(PacketDistributor.NEAR.with(() -> point), new LockSyncPacket(pos, locked));
        NetworkLD.CHANNEL.send(PacketDistributor.NEAR.with(() -> point), new LockSyncPacket(pos.above(), locked));
    }
}