package ru.givler.lastdawn.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import ru.givler.lastdawn.LastDawn;
import ru.givler.lastdawn.registry.BlockRegistration;

@Mod.EventBusSubscriber(
        modid = LastDawn.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientSetupHandler {

    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(BlockRegistration.GHOST_WALL.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistration.TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistration.WALL_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistration.BURNED_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistration.BURNED_WALL_TORCH.get(), RenderType.cutout());
    }
}