package ru.givler.lastdawn.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import ru.givler.lastdawn.item.SanityItem;
import ru.givler.lastdawn.sanity.SanityStage;

public class SanityItemTracker {

    private static final String TAG_ORIGINAL = "sanity_original_item";
    private static final String TAG_TRANSITIONING = "sanity_transitioning";

    /**
     * Вызывается из ServerEventHandler.onPlayerTick,
     * когда стадия изменилась — аналогично SanityBlockTracker.onStageChanged.
     */
    public static void onStageChanged(Player player, SanityStage oldStage, SanityStage newStage) {
        var inventory = player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) continue;

            // Случай 1: в руке SanityItem — проверяем нужно ли заменить
            if (stack.getItem() instanceof SanityItem sanityItem) {
                boolean wasGhost = sanityItem.isGhostStage(oldStage);
                boolean isGhost  = sanityItem.isGhostStage(newStage);

                if (!wasGhost && isGhost) {
                    replaceWithGhost(inventory, i, stack, sanityItem);
                }
                continue;
            }

            // Случай 2: в руке уже «призрак» — проверяем нужно ли вернуть оригинал
            if (stack.hasTag() && stack.getTag().contains(TAG_ORIGINAL)) {
                String originalId = stack.getTag().getString(TAG_ORIGINAL);
                // Находим, чьим ghostItem является этот стек
                // Ищем SanityItem с таким ghostItem в реестре не нужно —
                // оригинал записан в NBT, восстанавливаем по нему
                restoreOriginalIfNeeded(player, inventory, i, stack, newStage, originalId);
            }
        }
    }

    private static void replaceWithGhost(net.minecraft.world.entity.player.Inventory inventory,
                                         int slot, ItemStack original, SanityItem sanityItem) {
        ItemStack ghostStack = new ItemStack(sanityItem.getGhostItem(), original.getCount());

        // Переносим NBT оригинала
        if (original.hasTag()) {
            ghostStack.setTag(original.getTag().copy());
        }

        // Сохраняем id оригинала, чтобы потом восстановить
        ghostStack.getOrCreateTag().putString(TAG_ORIGINAL,
                net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getKey(sanityItem).toString());

        // Флаг анимации для клиента
        if (sanityItem.hasTransitionAnimation()) {
            ghostStack.getOrCreateTag().putBoolean(TAG_TRANSITIONING, true);
        }

        inventory.setItem(slot, ghostStack);
    }

    private static void restoreOriginalIfNeeded(Player player,
                                                net.minecraft.world.entity.player.Inventory inventory,
                                                int slot, ItemStack ghostStack,
                                                SanityStage newStage, String originalId) {
        // Парсим оригинальный предмет из NBT
        var optItem = net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getOptional(new net.minecraft.resources.ResourceLocation(originalId));
        if (optItem.isEmpty()) return;

        net.minecraft.world.item.Item originalItem = optItem.get();

        // Если это SanityItem и текущая стадия всё ещё ghost — не восстанавливаем
        if (originalItem instanceof SanityItem sanityItem && sanityItem.isGhostStage(newStage)) return;

        // Восстанавливаем оригинал
        ItemStack restoredStack = new ItemStack(originalItem, ghostStack.getCount());
        if (ghostStack.hasTag()) {
            var tag = ghostStack.getTag().copy();
            tag.remove(TAG_ORIGINAL);
            tag.remove(TAG_TRANSITIONING);
            if (!tag.isEmpty()) restoredStack.setTag(tag);
        }

        inventory.setItem(slot, restoredStack);
    }
}