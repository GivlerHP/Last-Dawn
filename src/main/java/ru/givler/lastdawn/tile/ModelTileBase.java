package ru.givler.lastdawn.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import ru.givler.lastdawn.registry.BlockEntityRegistration;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ModelTileBase extends BlockEntity implements GeoBlockEntity {

    private String textureName;
    private String modelName;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ModelTileBase(BlockPos pos, BlockState state, String textureName, String modelName) {
        super(BlockEntityRegistration.MODEL_TILE.get(), pos, state);
        this.textureName = textureName;
        this.modelName = modelName;
    }

    // ─── GeckoLib ─────────────────────────────────────────────────────────────

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Статичная модель — контроллеры не нужны
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // ─── Геттеры для GeoModel в рендерере ─────────────────────────────────────

    public String getTextureName() { return textureName; }
    public String getModelName()   { return modelName; }

    // ─── NBT ──────────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("textureName", textureName);
        tag.putString("modelName", modelName);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        // textureName и modelName — final, нельзя переназначить
        // нужно сделать их не final
    }
}