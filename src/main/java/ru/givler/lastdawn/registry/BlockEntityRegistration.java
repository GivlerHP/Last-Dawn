package ru.givler.lastdawn.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.givler.lastdawn.LastDawn;
import ru.givler.lastdawn.block.BlockModels;
import ru.givler.lastdawn.tile.ModelTileBase;

public class BlockEntityRegistration {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, LastDawn.MODID);

    public static final RegistryObject<BlockEntityType<ModelTileBase>> MODEL_TILE =
            BLOCK_ENTITIES.register("model_tile_base", () ->
                    BlockEntityType.Builder.of(
                            (pos, state) -> {
                                // Берём textureName и modelName из самого блока
                                if (state.getBlock() instanceof BlockModels blockModels) {
                                    return new ModelTileBase(pos, state,
                                            blockModels.getTextureName(),
                                            blockModels.getModelName());
                                }
                                return new ModelTileBase(pos, state, "", "");
                            },
                            ModelRegistration.BOOK_1.get(),
                            ModelRegistration.BOOK_2.get(),
                            ModelRegistration.BOOK_3.get(),
                            ModelRegistration.BOOK_4.get(),
                            ModelRegistration.BOOK_5.get(),
                            ModelRegistration.BOOK_6.get(),
                            ModelRegistration.BOOK_7.get(),
                            ModelRegistration.BOOK_8.get()
                    ).build(null)
            );
}