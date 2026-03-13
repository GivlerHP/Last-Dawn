package ru.givler.lastdawn.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import ru.givler.lastdawn.registry.ItemRegistration;

import java.util.List;

public class TorchItem extends Item {

    public static final String NBT_DURABILITY = "TorchDurability";
    public static final int MAX_DURABILITY = 1200; // 60 секунд (20 тиков * 60)

    public TorchItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createTorch() {
        ItemStack stack = new ItemStack(ru.givler.lastdawn.registry.BlockRegistration.TORCH.get().asItem());
        stack.getOrCreateTag().putInt(NBT_DURABILITY, MAX_DURABILITY);
        return stack;
    }

    public static ItemStack createTorchWithDurability(int durability) {
        ItemStack stack = new ItemStack(ru.givler.lastdawn.registry.BlockRegistration.TORCH.get().asItem());
        stack.getOrCreateTag().putInt(NBT_DURABILITY, durability);
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

    // Подсказка с прочностью
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        int dur = getDurability(stack);
        int seconds = dur / 20;
        tooltip.add(Component.literal("Осталось: " + seconds + "с"));
    }

}