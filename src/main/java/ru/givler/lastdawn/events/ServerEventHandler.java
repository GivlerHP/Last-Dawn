package ru.givler.lastdawn.events;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import ru.givler.lastdawn.LastDawn;
import ru.givler.lastdawn.item.LockingKeyItem;
import ru.givler.lastdawn.item.TorchItem;
import ru.givler.lastdawn.network.NetworkLD;
import ru.givler.lastdawn.network.packet.SanitySyncPacket;
import ru.givler.lastdawn.registry.BlockRegistration;
import ru.givler.lastdawn.registry.CommandRegistry;
import ru.givler.lastdawn.sanity.ISanity;
import ru.givler.lastdawn.sanity.SanityProvider;
import ru.givler.lastdawn.sanity.SanityStage;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraft.world.level.block.TrapDoorBlock;
import ru.givler.lastdawn.mechanics.LockManager;
import ru.givler.lastdawn.registry.ItemRegistration;

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

        tickTorch(player);
        tickDynamicLight(player);

        player.getCapability(SanityProvider.SANITY_CAP).ifPresent(sanity -> {
            SanityStage oldStage = sanity.getPreviousStage(); // ← берём сохранённую

            debugMessage(player, sanity);
            lightSanityChange(player, sanity);

            SanityStage newStage = sanity.getStage();

            if (oldStage != newStage && player instanceof ServerPlayer serverPlayer) {
                System.out.println("Stage changed: " + oldStage + " -> " + newStage);
                SanityBlockTracker.onStageChanged(serverPlayer, sanity, oldStage, newStage);
                sanity.setPreviousStage(newStage); // ← сохраняем новую
            }

            switch (newStage) {
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
            NetworkLD.CHANNEL.sendTo(
                    new SanitySyncPacket(sanity.getSanity()),
                    serverPlayer.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
        }
    }

    private static void tickTorch(Player player) {
        for (var hand : net.minecraft.world.InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof TorchItem)) continue;
            if (TorchItem.isBurned(stack)) continue;

            if (player.tickCount % 20 == 0) {
                int dur = TorchItem.getDurability(stack);
                TorchItem.setDurability(stack, dur - 20);

                if (TorchItem.isBurned(stack)) {
                    player.setItemInHand(hand, new ItemStack(BlockRegistration.BURNED_TORCH.get()));
                }
            }
        }
    }

    private static final java.util.Map<java.util.UUID, BlockPos> lastLightPos = new java.util.HashMap<>();

    private static void tickDynamicLight(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        boolean hasTorch = (main.getItem() instanceof TorchItem && !TorchItem.isBurned(main))
                || (off.getItem() instanceof TorchItem && !TorchItem.isBurned(off));

        BlockPos currentPos = player.blockPosition();
        BlockPos lastPos = lastLightPos.get(player.getUUID());

        if (hasTorch) {
            if (serverLevel.getBlockState(currentPos).isAir()) {
                serverLevel.setBlock(currentPos, net.minecraft.world.level.block.Blocks.LIGHT.defaultBlockState()
                        .setValue(LightBlock.LEVEL, 14), 3);
            }
            if (lastPos != null && !lastPos.equals(currentPos)) {
                BlockState lastState = serverLevel.getBlockState(lastPos);
                if (lastState.is(net.minecraft.world.level.block.Blocks.LIGHT)) {
                    serverLevel.setBlock(lastPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                }
            }
            lastLightPos.put(player.getUUID(), currentPos);
        } else {
            if (lastPos != null) {
                BlockState lastState = serverLevel.getBlockState(lastPos);
                if (lastState.is(net.minecraft.world.level.block.Blocks.LIGHT)) {
                    serverLevel.setBlock(lastPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                }
                lastLightPos.remove(player.getUUID());
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (!LockingKeyItem.isLockable(state)) return;

        boolean holdingLockItem = event.getItemStack().getItem() instanceof LockingKeyItem;
        if (holdingLockItem) return;

        BlockPos normalizedPos = LockingKeyItem.getNormalizedPos(level, pos, state);

        if (LockManager.isLocked(normalizedPos, level)) {
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        LockManager.syncToPlayer(serverPlayer);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandRegistry.registerAll(event.getDispatcher());
    }
}
