package ru.givler.lastdawn.mechanics;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.server.ServerLifecycleHooks;
import ru.givler.lastdawn.network.NetworkLD;
import ru.givler.lastdawn.network.packet.LockSyncPacket;

import java.util.HashSet;
import java.util.Set;

public class LockManager extends SavedData {

    private static final String DATA_NAME = "lockmod_locked_hatches";
    private final Set<Long> lockedPositions = new HashSet<>();
    private static final Set<Long> clientLockedPositions = new HashSet<>();

    public static void setClientLocked(BlockPos pos, boolean locked) {
        if (locked) clientLockedPositions.add(pos.asLong());
        else clientLockedPositions.remove(pos.asLong());
    }
    // SavedData

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (long pos : lockedPositions) {
            list.add(LongTag.valueOf(pos));
        }
        tag.put("locked", list);
        return tag;
    }

    public static LockManager load(CompoundTag tag) {
        LockManager manager = new LockManager();
        ListTag list = tag.getList("locked", 4); // 4 = LongTag type
        for (int i = 0; i < list.size(); i++) {
            manager.lockedPositions.add(((LongTag) list.get(i)).getAsLong());
        }
        return manager;
    }

    private static LockManager get(Level level) {
        if (level.isClientSide()) return null;
        DimensionDataStorage storage = ServerLifecycleHooks.getCurrentServer()
                .overworld().getDataStorage();
        return storage.computeIfAbsent(LockManager::load, LockManager::new, DATA_NAME);
    }

    public static void syncToPlayer(ServerPlayer player) {
        LockManager mgr = get(player.level());
        if (mgr == null) return;

        for (long posLong : mgr.lockedPositions) {
            BlockPos pos = BlockPos.of(posLong);
            NetworkLD.CHANNEL.sendTo(
                    new LockSyncPacket(pos, true),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
        }
    }

    // Public API
    public static boolean isLocked(BlockPos pos, Level level) {
        if (level.isClientSide()) {
            return clientLockedPositions.contains(pos.asLong());
        }
        LockManager mgr = get(level);
        return mgr != null && mgr.lockedPositions.contains(pos.asLong());
    }
    public static void lock(BlockPos pos, Level level) {
        LockManager mgr = get(level);
        if (mgr != null) {
            mgr.lockedPositions.add(pos.asLong());
            mgr.setDirty();
        }
    }

    public static void unlock(BlockPos pos, Level level) {
        LockManager mgr = get(level);
        if (mgr != null) {
            mgr.lockedPositions.remove(pos.asLong());
            mgr.setDirty();
        }
    }
}