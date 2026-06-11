package ru.givler.lastdawn.client.renderer.item;

import net.minecraft.resources.ResourceLocation;
import ru.givler.lastdawn.LastDawn;
import ru.givler.lastdawn.item.ModelItemBase;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ModelItemBaseRenderer extends GeoItemRenderer<ModelItemBase> {

    public ModelItemBaseRenderer() {
        super(new ModelItemBaseGeoModel());
    }

    private static class ModelItemBaseGeoModel extends GeoModel<ModelItemBase> {

        @Override
        public ResourceLocation getModelResource(ModelItemBase animatable) {
            return new ResourceLocation(LastDawn.MODID,
                    "geo/" + animatable.getModelName() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(ModelItemBase animatable) {
            return new ResourceLocation(LastDawn.MODID,
                    "textures/model/" + animatable.getTextureName() + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(ModelItemBase animatable) {
            return null;
        }
    }
}