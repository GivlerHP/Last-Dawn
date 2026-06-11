package ru.givler.lastdawn.client.renderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import ru.givler.lastdawn.LastDawn;
import ru.givler.lastdawn.tile.ModelTileBase;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * Рендерер для всех статичных GeckoLib-моделей (GeckoLib 4.x, Forge 1.20.1).
 *
 * Регистрация — в клиентском классе через @SubscribeEvent:
 *
 *   @Mod.EventBusSubscriber(modid = MBOMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
 *   public class ClientEvents {
 *       @SubscribeEvent
 *       public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
 *           event.registerBlockEntityRenderer(ModBlockEntities.MODEL_TILE.get(), ModelTileBaseRenderer::new);
 *       }
 *   }
 */
public class ModelTileBaseRenderer extends GeoBlockRenderer<ModelTileBase> {

    public ModelTileBaseRenderer(BlockEntityRendererProvider.Context context) {
        super(new ModelTileBaseGeoModel());
    }

    private static class ModelTileBaseGeoModel extends GeoModel<ModelTileBase> {

        @Override
        public ResourceLocation getModelResource(ModelTileBase animatable) {
            // assets/<modid>/geo/<modelName>.geo.json
            return new ResourceLocation(LastDawn.MODID,
                    "geo/" + animatable.getModelName() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(ModelTileBase animatable) {
            // assets/<modid>/textures/block/<textureName>.png
            return new ResourceLocation(LastDawn.MODID,
                    "textures/model/" + animatable.getTextureName() + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(ModelTileBase animatable) {
            // Для анимаций: assets/<modid>/animations/<modelName>.animation.json
            //пока что не надо
            return null;
        }
    }
}