package ru.givler.lastdawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleOptions;
import ru.givler.lastdawn.entity.TorchBlockEntity;

import javax.annotation.Nullable;
import java.util.Properties;

public class LDTorchBlock extends TorchBlock implements EntityBlock {

    public LDTorchBlock(Properties properties, ParticleOptions particle) {
        super(properties, particle);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TorchBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,
                                                                  BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof TorchBlockEntity torchBE) torchBE.tick();
        };
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean moved) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TorchBlockEntity torchBE) {
                ItemStack drop = torchBE.getDurability() <= 0
                        ? new ItemStack(ru.givler.lastdawn.registry.BlockRegistration.BURNED_TORCH.get().asItem())
                        : ru.givler.lastdawn.item.TorchItem.createTorchWithDurability(torchBE.getDurability());
                net.minecraft.world.entity.item.ItemEntity itemEntity =
                        new net.minecraft.world.entity.item.ItemEntity(
                                level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                level.addFreshEntity(itemEntity);
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}