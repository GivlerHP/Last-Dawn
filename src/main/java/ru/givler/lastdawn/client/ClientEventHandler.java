package ru.givler.lastdawn.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.givler.lastdawn.LastDawn;
import ru.givler.lastdawn.sanity.SanityProvider;
import ru.givler.lastdawn.sanity.SanityStage;

@Mod.EventBusSubscriber(
        modid = LastDawn.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public class ClientEventHandler {

    private static final ResourceLocation VIGNETTE_TEXTURE =
            new ResourceLocation("minecraft", "textures/misc/vignette.png"); //standart texture

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        player.getCapability(SanityProvider.SANITY_CAP).ifPresent(sanity -> {
            int s = sanity.getSanity();
            if (s <= 0) return;

            SanityStage stage = sanity.getStage();
            float intensity = s / (float) sanity.getMaxSanity();

            intensity = (s / (float) sanity.getMaxSanity()) * 0.5f;

            float pulse = switch (stage) {
                case SANE      -> 1.0f;
                case ANXIOUS   -> 0.93f + 0.07f * (float) Math.sin(player.tickCount * 0.05f);
                case PSYCHOSIS -> 0.88f + 0.12f * (float) Math.sin(player.tickCount * 0.08f);
                case INSANITY  -> 0.80f + 0.20f * (float) Math.sin(player.tickCount * 0.12f);
            };

            intensity *= pulse;

            intensity = Math.min(intensity, 0.85f);

            renderVignette(event.getGuiGraphics(), intensity);
        });
    }

    private static void renderVignette(GuiGraphics gui, float intensity) {
        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        gui.setColor(0f, 0f, 0f, intensity * 0.7f);
        gui.blit(VIGNETTE_TEXTURE, 0, 0, 0f, 0f, w, h, w, h);
        gui.setColor(1f, 1f, 1f, 1f);

        RenderSystem.disableBlend();
    }
}


