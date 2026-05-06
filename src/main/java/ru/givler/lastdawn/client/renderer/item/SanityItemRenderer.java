package ru.givler.lastdawn.client.renderer.item;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SanityItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final int TRANSITION_TICKS = 15;
    private static final String TAG = "sanity_transitioning";

    // Храним таймер для каждого слота инвентаря (по hashCode стека — упрощённо)
    private static final Map<Integer, Integer> timers = new HashMap<>();

    public SanityItemRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels()
        );
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx,
                             PoseStack pose, MultiBufferSource buffers,
                             int light, int overlay) {

        boolean transitioning = stack.hasTag() && stack.getTag().getBoolean(TAG);

        if (transitioning) {
            int id = System.identityHashCode(stack);
            int remaining = timers.getOrDefault(id, TRANSITION_TICKS);
            float progress = 1f - (remaining / (float) TRANSITION_TICKS); // 0..1

            pose.pushPose();
            applyTransitionEffect(pose, progress);

            renderItemDefault(stack, ctx, pose, buffers, light, overlay);

            pose.popPose();

            if (remaining <= 0) {
                stack.getOrCreateTag().remove(TAG);
                timers.remove(id);
            } else {
                timers.put(id, remaining - 1);
            }

        } else {
            renderItemDefault(stack, ctx, pose, buffers, light, overlay);
        }
    }

    private void applyTransitionEffect(PoseStack pose, float progress) {
        // Тряска — максимум в середине анимации
        float shake = (float) Math.sin(progress * Math.PI) * 0.08f;
        float offsetX = (float) (Math.sin(progress * Math.PI * 6) * shake);
        float offsetY = (float) (Math.cos(progress * Math.PI * 4) * shake * 0.5f);
        pose.translate(offsetX, offsetY, 0);

        float rot = (float) Math.sin(progress * Math.PI) * 8f;
        pose.mulPose(Axis.ZP.rotationDegrees(rot));

        float scale = 1f - 0.25f * (float) Math.sin(progress * Math.PI);
        pose.scale(scale, scale, scale);
    }

    private void renderItemDefault(ItemStack stack, ItemDisplayContext ctx,
                                   PoseStack pose, MultiBufferSource buffers,
                                   int light, int overlay) {
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack, ctx, light, overlay, pose, buffers, null, 0
        );
    }
}