package ru.givler.lastdawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import ru.givler.lastdawn.item.TorchItem;
import ru.givler.lastdawn.registry.BlockRegistration;

public class TorchBlockLD extends TorchBlock {

    public static final int TICK_INTERVAL = 100;
    public static final int MAX_STEPS = 12;
    public static final IntegerProperty BURNTIME = IntegerProperty.create("burntime", 0, MAX_STEPS);

    public TorchBlockLD(Properties properties, ParticleOptions particle) {
        super(properties, particle);
        registerDefaultState(stateDefinition.any().setValue(BURNTIME, MAX_STEPS));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BURNTIME);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        int durability = TorchItem.getDurability(stack);
        int steps = Math.max(1, (int) Math.ceil((double) durability / TICK_INTERVAL));
        level.setBlock(pos, state.setValue(BURNTIME, Math.min(steps, MAX_STEPS)), 3);
        level.scheduleTick(pos, this, TICK_INTERVAL);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int current = state.getValue(BURNTIME);
        if (current <= 0) return;

        int next = current - 1;
        if (next <= 0) {
            level.setBlock(pos, BlockRegistration.BURNED_TORCH.get().defaultBlockState(), 3);
        } else {
            level.setBlock(pos, state.setValue(BURNTIME, next), 3);
            level.scheduleTick(pos, this, TICK_INTERVAL);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean moved) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            int steps = state.getValue(BURNTIME);
            ItemStack drop;
            if (steps <= 0 || state.is(BlockRegistration.BURNED_TORCH.get())) {
                drop = new ItemStack(BlockRegistration.BURNED_TORCH.get().asItem());
            } else {
                int durability = steps * TICK_INTERVAL;
                drop = TorchItem.createTorchWithDurability(durability);
            }

            if (!newState.is(BlockRegistration.BURNED_TORCH.get())) {
                ItemEntity itemEntity = new ItemEntity(
                        level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                level.addFreshEntity(itemEntity);
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}