package ru.givler.lastdawn.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.givler.lastdawn.LastDawn;
import ru.givler.lastdawn.block.BlockModels;
import ru.givler.lastdawn.item.ModelItemBase;

import java.util.function.Supplier;

public class ModelRegistration {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, LastDawn.MODID);

    public static final RegistryObject<Block> BOOK_1 = registerWithItem("book_1",
            () -> new BlockModels(
                    BlockBehaviour.Properties.of().strength(1.0f),
                    "book_1",  // имя текстуры: textures/block/test.png
                    "book_1"   // имя модели:   geo/test.geo.json
            )
    );


    public static final RegistryObject<Block> BOOK_2 = registerWithItem("book_2",
            () -> new BlockModels(
                    BlockBehaviour.Properties.of().strength(1.0f),
                    "book_2",
                    "book_2"
            )
    );

    public static final RegistryObject<Block> BOOK_3 = registerWithItem("book_3",
            () -> new BlockModels(
                    BlockBehaviour.Properties.of().strength(1.0f),
                    "book_3",
                    "book_3"
            )
    );

    public static final RegistryObject<Block> BOOK_4 = registerWithItem("book_4",
            () -> new BlockModels(
                    BlockBehaviour.Properties.of().strength(1.0f),
                    "book_4",
                    "book_4"
            )
    );

    public static final RegistryObject<Block> BOOK_5 = registerWithItem("book_5",
            () -> new BlockModels(
                    BlockBehaviour.Properties.of().strength(1.0f),
                    "book_5",
                    "book_5"
            )
    );

    public static final RegistryObject<Block> BOOK_6 = registerWithItem("book_6",
            () -> new BlockModels(
                    BlockBehaviour.Properties.of().strength(1.0f),
                    "book_6",
                    "book_6"
            )
    );

    public static final RegistryObject<Block> BOOK_7 = registerWithItem("book_7",
            () -> new BlockModels(
                    BlockBehaviour.Properties.of().strength(1.0f),
                    "book_7",
                    "book_7"
            )
    );

    public static final RegistryObject<Block> BOOK_8 = registerWithItem("book_8",
            () -> new BlockModels(
                    BlockBehaviour.Properties.of().strength(1.0f),
                    "book_8",
                    "book_8"
            )
    );

    private static <T extends Block> RegistryObject<T> registerWithItem(String name, Supplier<T> block) {
        RegistryObject<T> registered = BLOCKS.register(name, block);
        ItemRegistration.ITEMS.register(name, () -> {
            Block b = registered.get();
            if (b instanceof BlockModels blockModels)
                return new ModelItemBase(blockModels, new Item.Properties(), name, name);
            return new BlockItem(b, new Item.Properties());
        });
        return registered;
    }
}