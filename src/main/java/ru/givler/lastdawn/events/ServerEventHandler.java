package ru.givler.lastdawn.events;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import ru.givler.lastdawn.LastDawn;
import ru.givler.lastdawn.network.LDNetwork;
import ru.givler.lastdawn.network.packet.SanitySyncPacket;
import ru.givler.lastdawn.registry.CommandRegistry;
import ru.givler.lastdawn.sanity.ISanity;
import ru.givler.lastdawn.sanity.SanityProvider;

/**
 * Содержит обработчики событий: деградация рассудка, восстановление, триггер стадий и спавн вардена.
 */
@Mod.EventBusSubscriber(modid = LastDawn.MODID)
public class ServerEventHandler {
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
            debugMessage(player, sanity);
            lightSanityChange(player, sanity);

            switch (sanity.getStage()) {
                case ANXIOUS   -> anxious(player);
                case PSYCHOSIS -> psychosis(player, sanity);
                case INSANITY  -> insanity(player, sanity);
            }
        });
    }

    private static void debugMessage(Player player, ISanity sanity) {
        if (player.tickCount % 100 == 0) {
            player.sendSystemMessage(
                    Component.literal("Рассудок: " + sanity.getSanity())
            );
        }
    }

    private static void lightSanityChange(Player player, ISanity sanity) {
        if (player.tickCount % 20 != 0) return;

        int light = player.level().getMaxLocalRawBrightness(player.blockPosition());
        if (light < 7) {
            sanity.addSanity(1);
        } else if (sanity.getSanity() > 0) {
            sanity.reduceSanity(1);
        }
        syncSanity(player, sanity);
    }

    private static void anxious(Player player) {
        if (player.tickCount % 200 != 0) return;

        SoundEvent[] scarySounds = {
                SoundEvents.GHAST_SCREAM,
                SoundEvents.AMBIENT_UNDERWATER_ENTER
        };

        SoundEvent scary = scarySounds[player.getRandom().nextInt(scarySounds.length)];
        player.level().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                scary,
                SoundSource.PLAYERS,
                1.0F, 1.0F
        );
    }

    private static void psychosis(Player player, ISanity sanity) {

    }

    private static void insanity(Player player, ISanity sanity) {
        if (player.tickCount % 200 == 0) {
            player.hurt(player.damageSources().magic(), 1.0F);
        }

        if (!(player instanceof ServerPlayer)) return;

        if (sanity.getSanity() >= 100 && !sanity.hasSpawnedWarden()) {
            spawnMonster(player);
            sanity.setSpawnedWarden(true);
        } else if (sanity.getSanity() < 100 && sanity.hasSpawnedWarden()) {
            sanity.setSpawnedWarden(false);
        }
    }

    private static void spawnMonster(Player player) {
        Level world = player.level();
        Warden warden = EntityType.WARDEN.create(world);
        if (warden != null) {
            warden.moveTo(player.getX(), player.getY(), player.getZ());
            world.addFreshEntity(warden);
        }
    }

    private static void syncSanity(Player player, ISanity sanity) {
        if (player instanceof ServerPlayer serverPlayer) {
            LDNetwork.CHANNEL.sendTo(
                    new SanitySyncPacket(sanity.getSanity()),
                    serverPlayer.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandRegistry.registerAll(event.getDispatcher());
    }
}
