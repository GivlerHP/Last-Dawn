package ru.givler.lastdawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import ru.givler.lastdawn.item.TorchItem;
import ru.givler.lastdawn.registry.BlockRegistration;

public class WallTorchBlockLD extends WallTorchBlock {

    public WallTorchBlockLD(Properties properties, ParticleOptions particle) {
        super(properties, particle);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TorchBlockLD.BURNTIME, TorchBlockLD.MAX_STEPS));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TorchBlockLD.BURNTIME);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        int durability = TorchItem.getDurability(stack);
        int steps = Math.max(1, (int) Math.ceil((double) durability / TorchBlockLD.TICK_INTERVAL));
        level.setBlock(pos, state.setValue(TorchBlockLD.BURNTIME, Math.min(steps, TorchBlockLD.MAX_STEPS)), 3);
        level.scheduleTick(pos, this, TorchBlockLD.TICK_INTERVAL);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int current = state.getValue(TorchBlockLD.BURNTIME);
        if (current <= 0) return;

        int next = current - 1;
        if (next <= 0) {
            level.setBlock(pos, BlockRegistration.BURNED_WALL_TORCH.get().defaultBlockState()
                    .setValue(FACING, state.getValue(FACING)), 3);
        } else {
            level.setBlock(pos, state.setValue(TorchBlockLD.BURNTIME, next), 3);
            level.scheduleTick(pos, this, TorchBlockLD.TICK_INTERVAL);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean moved) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            if (newState.is(BlockRegistration.BURNED_WALL_TORCH.get())) {
                super.onRemove(state, level, pos, newState, moved);
                return;
            }

            int steps = state.getValue(TorchBlockLD.BURNTIME);
            ItemStack drop;
            if (steps <= 0) {
                drop = new ItemStack(BlockRegistration.BURNED_TORCH.get().asItem());
            } else {
                int durability = steps * TorchBlockLD.TICK_INTERVAL;
                drop = TorchItem.createTorchWithDurability(durability);
            }

            ItemEntity itemEntity = new ItemEntity(
                    level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
            level.addFreshEntity(itemEntity);
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}