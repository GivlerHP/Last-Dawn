package ru.givler.lastdawn.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.givler.lastdawn.LastDawn;
import ru.givler.lastdawn.block.SanityBlock;
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
}