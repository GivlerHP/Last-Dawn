package ru.givler.lastdawn.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.givler.lastdawn.LastDawn;
import ru.givler.lastdawn.sanity.SanityProvider;
import ru.givler.lastdawn.sanity.SanityStage;
import ru.givler.lastdawn.sanity.Sanity;

import java.util.Random;

/**
 * Содержит обработчики событий: деградация рассудка, восстановление, триггер стадий и спавн вардена.
 */
@Mod.EventBusSubscriber(modid = LastDawn.MODID)
public class SanityEventHandler {
    // Привязка capability к каждому игроку
    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(LastDawn.MODID, "sanity"), new SanityProvider());
        }
    }

    @SubscribeEvent
    public static void playerClone(PlayerEvent.Clone event) {
        event.getOriginal().getCapability(SanityProvider.SANITY_CAP).ifPresent(oldSanity -> {
            event.getEntity().getCapability(SanityProvider.SANITY_CAP).ifPresent(newSanity -> {
                newSanity.setSanity(oldSanity.getSanity());
            });
        });
    }
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;

        player.getCapability(SanityProvider.SANITY_CAP).ifPresent(sanity -> {
            if (player.tickCount % 100 == 0) {
                player.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("Рассудок: " + sanity.getSanity())
                );
            }

            int light = player.level().getMaxLocalRawBrightness(player.blockPosition());
            if (player.tickCount % 20 == 0) {
                if (light < 7) {
                    sanity.addSanity(1);
                } else if (sanity.getSanity() > 0) {
                    sanity.reduceSanity(1);
                }
            }

            SanityStage stage = sanity.getStage();
            switch (stage) {
                case ANXIOUS:
                    if (player.tickCount % 200 == 0) {
                        net.minecraft.sounds.SoundEvent[] scarySounds = {
                                net.minecraft.sounds.SoundEvents.GHAST_SCREAM,
                                net.minecraft.sounds.SoundEvents.AMBIENT_UNDERWATER_ENTER
                        };

                        int idx = player.getRandom().nextInt(scarySounds.length);
                        net.minecraft.sounds.SoundEvent scary = scarySounds[idx];

                        player.level().playSound(
                                null,
                                player.getX(), player.getY(), player.getZ(),
                                scary,
                                net.minecraft.sounds.SoundSource.PLAYERS,
                                1.0F, 1.0F
                        );
                    }
                    break;
                case PSYCHOSIS:
                    break;
                case INSANITY:
                    if (player.tickCount % 200 == 0) {
                        player.hurt(player.damageSources().magic(), 1.0F);
                    }
                    if (sanity.getSanity() >= 100 && !sanity.hasSpawnedWarden()) {
                        if (player instanceof ServerPlayer) {
                            Level world = player.level();
                            Warden warden = EntityType.WARDEN.create(world);
                            if (warden != null) {
                                warden.moveTo(player.getX(), player.getY(), player.getZ());
                                world.addFreshEntity(warden);
                            }
                            sanity.setSpawnedWarden(true);
                        }
                    }
                    if (sanity.getSanity() < 100 && sanity.hasSpawnedWarden()) {
                        sanity.setSpawnedWarden(false);
                    }
                    break;
                default:

            }
        });
    }
}
