package ru.givler.lastdawn.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.givler.lastdawn.LastDawn;
import ru.givler.lastdawn.entity.TorchBlockEntity;

public class BlockEntityRegistration {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, LastDawn.MODID);

    public static final RegistryObject<BlockEntityType<TorchBlockEntity>> TORCH_BE =
            BLOCK_ENTITIES.register("torch",
                    () -> BlockEntityType.Builder
                            .of(TorchBlockEntity::new,
                                    BlockRegistration.TORCH.get(),
                                    BlockRegistration.WALL_TORCH.get())
                            .build(null)
            );
}