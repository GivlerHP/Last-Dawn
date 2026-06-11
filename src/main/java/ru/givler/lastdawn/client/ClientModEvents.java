// Отдельный файл: ClientModEvents.java
package ru.givler.lastdawn.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.givler.lastdawn.LastDawn;
import ru.givler.lastdawn.client.renderer.ModelTileBaseRenderer;
import ru.givler.lastdawn.registry.BlockEntityRegistration;

@Mod.EventBusSubscriber(modid = LastDawn.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                BlockEntityRegistration.MODEL_TILE.get(),
                ctx -> new ModelTileBaseRenderer(ctx)
        );
    }
}