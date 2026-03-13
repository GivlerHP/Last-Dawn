package ru.givler.lastdawn.registry;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.givler.lastdawn.LastDawn;
import ru.givler.lastdawn.block.LDWallTorchBlock;
import ru.givler.lastdawn.block.SanityBlock;
import ru.givler.lastdawn.block.LDTorchBlock;
import ru.givler.lastdawn.sanity.SanityStage;

import java.util.function.Supplier;

public class BlockRegistration {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, LastDawn.MODID);

    public static final RegistryObject<Block> GHOST_WALL = registerWithItem("ghost_wall",
            () -> new SanityBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.0f)
                            .noOcclusion()
                            .isSuffocating((s, l, p) -> false)
                            .isViewBlocking((s, l, p) -> false),
                    SanityStage.INSANITY
            )
    );

    private static <T extends Block> RegistryObject<T> registerWithItem(String name, Supplier<T> block) {
        RegistryObject<T> registered = BLOCKS.register(name, block);
        ItemRegistration.ITEMS.register(name, () -> new BlockItem(registered.get(), new Item.Properties()));
        return registered;
    }

    public static final RegistryObject<Block> TORCH = registerWithItem("torch",
            () -> new LDTorchBlock(
                    BlockBehaviour.Properties.copy(Blocks.TORCH).noOcclusion(),
                    ParticleTypes.FLAME
            )
    );

    // Настенный факел (с огнём)
    public static final RegistryObject<Block> WALL_TORCH = BLOCKS.register("wall_torch",
            () -> new LDWallTorchBlock(
                    BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).noOcclusion(),
                    ParticleTypes.FLAME
            )
    );

    // Сгоревший напольный
    public static final RegistryObject<Block> BURNED_TORCH = registerWithItem("burned_torch",
            () -> new LDTorchBlock(
                    BlockBehaviour.Properties.copy(Blocks.TORCH).noOcclusion().lightLevel(s -> 0),
                    ParticleTypes.SMOKE
            )
    );

    // Сгоревший настенный
    public static final RegistryObject<Block> BURNED_WALL_TORCH = BLOCKS.register("burned_wall_torch",
            () -> new LDWallTorchBlock(
                    BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).noOcclusion().lightLevel(s -> 0),
                    ParticleTypes.SMOKE
            )
    );
}