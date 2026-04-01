package ru.givler.lastdawn.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TorchItem extends BlockItem {

    public static final String NBT_DURABILITY = "TorchDurability";
    public static final int MAX_DURABILITY = 1200;

    public TorchItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static ItemStack createTorch() {
        ItemStack stack = new ItemStack(ru.givler.lastdawn.registry.ItemRegistration.TORCH_ITEM.get());
        stack.getOrCreateTag().putInt(NBT_DURABILITY, MAX_DURABILITY);
        return stack;
    }

    public static ItemStack createTorchWithDurability(int durability) {
        ItemStack stack = new ItemStack(ru.givler.lastdawn.registry.ItemRegistration.TORCH_ITEM.get());
        stack.getOrCreateTag().putInt(NBT_DURABILITY, Math.max(0, durability));
        return stack;
    }

    public static int getDurability(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_DURABILITY)) return MAX_DURABILITY;
        return tag.getInt(NBT_DURABILITY);
    }

    public static void setDurability(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(NBT_DURABILITY, Math.max(0, value));
    }

    public static boolean isBurned(ItemStack stack) {
        return getDurability(stack) <= 0;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getDurability(stack) < MAX_DURABILITY;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getDurability(stack) / MAX_DURABILITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float ratio = (float) getDurability(stack) / MAX_DURABILITY;
        int r = (int) ((1.0f - ratio) * 255);
        int g = (int) (ratio * 255);
        return (r << 16) | (g << 8);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (slotChanged) return true;
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        int dur = getDurability(stack);
        int seconds = dur / 20;
        tooltip.add(Component.literal("Осталось: " + seconds + "с"));
    }
}