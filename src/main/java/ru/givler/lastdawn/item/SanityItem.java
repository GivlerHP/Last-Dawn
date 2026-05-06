package ru.givler.lastdawn.item;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import ru.givler.lastdawn.client.renderer.item.SanityItemRenderer;
import ru.givler.lastdawn.sanity.SanityProvider;
import ru.givler.lastdawn.sanity.SanityStage;

import java.util.Set;
import java.util.function.Consumer;

public class SanityItem extends Item {

    private final Set<SanityStage> ghostStages;
    private final Item ghostItem;
    private final boolean hasTransitionAnimation;

    public SanityItem(Properties properties, Item ghostItem,
                      boolean hasTransitionAnimation, SanityStage... ghostStages) {
        super(properties);
        this.ghostStages = Set.of(ghostStages);
        this.ghostItem = ghostItem;
        this.hasTransitionAnimation = hasTransitionAnimation;
    }

    /** Без анимации */
    public SanityItem(Properties properties, Item ghostItem, SanityStage... ghostStages) {
        this(properties, ghostItem, false, ghostStages);
    }


    public boolean isGhostStage(SanityStage stage) {
        return ghostStages.contains(stage);
    }

    public Set<SanityStage> getGhostStages() { return ghostStages; }
    public Item getGhostItem() { return ghostItem; }
    public boolean hasTransitionAnimation() { return hasTransitionAnimation; }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        if (!hasTransitionAnimation) return;
        consumer.accept(new IClientItemExtensions() {
            private SanityItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new SanityItemRenderer();
                return renderer;
            }
        });
    }

    public boolean isGhostFor(Player player) {
        return player.getCapability(SanityProvider.SANITY_CAP)
                .map(sanity -> ghostStages.contains(sanity.getStage()))
                .orElse(false);
    }
}