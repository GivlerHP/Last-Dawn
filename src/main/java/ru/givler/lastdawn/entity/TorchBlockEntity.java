package ru.givler.lastdawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import ru.givler.lastdawn.registry.BlockEntityRegistration;
import ru.givler.lastdawn.registry.BlockRegistration;

public class TorchBlockEntity extends BlockEntity {

    private static final String NBT_DURABILITY = "TorchDurability";
    private static final int MAX_DURABILITY = 1200; // 60 секунд

    private int durability = MAX_DURABILITY;
    private int tickCounter = 0;

    public TorchBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistration.TORCH_BE.get(), pos, state);
    }

    public static <T extends BlockEntity> BlockEntityTicker<T> getTicker() {
        return (level, pos, state, be) -> {
            if (be instanceof TorchBlockEntity torchBE) {
                torchBE.tick();
            }
        };
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        tickCounter++;
        if (tickCounter % 20 == 0) { // раз в секунду
            durability--;

            if (durability <= 0) {
                burnOut();
            }

            setChanged(); // помечаем что нужно сохранить
        }
    }

    private void burnOut() {
        if (level == null) return;
        BlockPos pos = getBlockPos();
        BlockState currentState = level.getBlockState(pos);

        if (currentState.is(BlockRegistration.TORCH.get())) {
            level.setBlock(pos, BlockRegistration.BURNED_TORCH.get().defaultBlockState(), 3);
        } else if (currentState.is(BlockRegistration.WALL_TORCH.get())) {
            level.setBlock(pos, BlockRegistration.BURNED_WALL_TORCH.get().defaultBlockState(), 3);
        }
    }

    public int getDurability() { return durability; }
    public void setDurability(int value) { durability = Math.max(0, value); }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(NBT_DURABILITY, durability);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(NBT_DURABILITY)) {
            durability = tag.getInt(NBT_DURABILITY);
        }
    }
}