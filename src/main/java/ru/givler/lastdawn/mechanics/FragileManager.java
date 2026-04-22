package ru.givler.lastdawn.mechanics;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.server.ServerLifecycleHooks;
import ru.givler.lastdawn.network.NetworkLD;
import ru.givler.lastdawn.network.packet.FragileSyncPacket;

import java.util.HashMap;
import java.util.Map;

public class FragileManager extends SavedData {

    private static final String DATA_NAME = "lastdawn_fragile_hatches";
    private final Map<Long, Integer> fragilePositions = new HashMap<>();
    private static final Map<Long, Integer> clientFragilePositions = new HashMap<>();

    // SavedData
    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (var entry : fragilePositions.entrySet()) {
            CompoundTag entry_tag = new CompoundTag();
            entry_tag.putLong("pos", entry.getKey());
            entry_tag.putInt("stage", entry.getValue());
            list.add(entry_tag);
        }
        tag.put("fragile", list);
        return tag;
    }

    public static FragileManager load(CompoundTag tag) {
        FragileManager manager = new FragileManager();
        ListTag list = tag.getList("fragile", 10); // 10 = CompoundTag
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            manager.fragilePositions.put(entry.getLong("pos"), entry.getInt("stage"));
        }
        return manager;
    }

    private static FragileManager get(Level level) {
        if (level.isClientSide()) return null;
        DimensionDataStorage storage = ServerLifecycleHooks.getCurrentServer()
                .overworld().getDataStorage();
        return storage.computeIfAbsent(FragileManager::load, FragileManager::new, DATA_NAME);
    }

    // Public API
    public static boolean isFragile(BlockPos pos, Level level) {
        if (level.isClientSide()) return clientFragilePositions.containsKey(pos.asLong());
        FragileManager mgr = get(level);
        return mgr != null && mgr.fragilePositions.containsKey(pos.asLong());
    }

    public static int getStage(BlockPos pos, Level level) {
        if (level.isClientSide()) return clientFragilePositions.getOrDefault(pos.asLong(), 0);
        FragileManager mgr = get(level);
        return mgr != null ? mgr.fragilePositions.getOrDefault(pos.asLong(), 0) : 0;
    }

    public static void setFragile(BlockPos pos, Level level) {
        FragileManager mgr = get(level);
        if (mgr != null) {
            mgr.fragilePositions.put(pos.asLong(), 1);
            mgr.setDirty();
        }
    }

    public static boolean hit(BlockPos pos, Level level) {
        FragileManager mgr = get(level);
        if (mgr == null) return false;

        int stage = mgr.fragilePositions.getOrDefault(pos.asLong(), 0);
        if (stage == 0) return false;

        if (stage >= 3) {
            mgr.fragilePositions.remove(pos.asLong());
            mgr.setDirty();
            return true;
        } else {
            mgr.fragilePositions.put(pos.asLong(), stage + 1);
            mgr.setDirty();
            return false;
        }
    }

    public static void remove(BlockPos pos, Level level) {
        FragileManager mgr = get(level);
        if (mgr != null) {
            mgr.fragilePositions.remove(pos.asLong());
            mgr.setDirty();
        }
    }

    public static void setClientStage(BlockPos pos, int stage) {
        if (stage <= 0) clientFragilePositions.remove(pos.asLong());
        else clientFragilePositions.put(pos.asLong(), stage);
    }

    public static void syncToPlayer(ServerPlayer player) {
        FragileManager mgr = get(player.level());
        if (mgr == null) return;

        for (var entry : mgr.fragilePositions.entrySet()) {
            BlockPos pos = BlockPos.of(entry.getKey());
            int stage = entry.getValue();

            NetworkLD.CHANNEL.sendTo(
                    new FragileSyncPacket(pos, stage),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );

            BlockState state = player.level().getBlockState(pos);
            if (state.getBlock() instanceof DoorBlock) {
                NetworkLD.CHANNEL.sendTo(
                        new FragileSyncPacket(pos.above(), stage),
                        player.connection.connection,
                        NetworkDirection.PLAY_TO_CLIENT
                );
            }
        }
    }

    public static Map<Long, Integer> getAll(Level level) {
        FragileManager mgr = get(level);
        return mgr != null ? mgr.fragilePositions : new HashMap<>();
    }

    public static Map<Long, Integer> getClientPositions() {
        return clientFragilePositions;
    }


}