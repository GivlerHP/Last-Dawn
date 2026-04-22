package ru.givler.lastdawn.sanity;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SanityProvider implements ICapabilitySerializable<CompoundTag> {

    public static final Capability<ISanity> SANITY_CAP =
            CapabilityManager.get(new CapabilityToken<>() {});

    private final ISanity instance = new Sanity();
    private final LazyOptional<ISanity> optional = LazyOptional.of(() -> instance);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
        return cap == SANITY_CAP ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("sanity", instance.getSanity());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.setSanity(nbt.getInt("sanity"));
    }
}
