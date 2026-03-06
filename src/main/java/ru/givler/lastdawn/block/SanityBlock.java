package ru.givler.lastdawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.*;
import ru.givler.lastdawn.sanity.SanityProvider;
import ru.givler.lastdawn.sanity.SanityStage;

import java.util.Set;

public class SanityBlock extends Block {

    private final Set<SanityStage> ghostStages;

    public SanityBlock(Properties properties, SanityStage... ghostStages) {
        super(properties);
        this.ghostStages = Set.of(ghostStages);
        registerDefaultState(this.stateDefinition.any().setValue(GHOST, false));
    }

    public boolean isGhostStage(SanityStage stage) {
        return ghostStages.contains(stage);
    }

    public Set<SanityStage> getGhostStages() {
        return ghostStages;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level,
                                        BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext ctx
                && ctx.getEntity() instanceof Player player
                && isGhostFor(player)) {
            return Shapes.empty();
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level,
                               BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext ctx
                && ctx.getEntity() instanceof Player player
                && isGhostFor(player)) {
            return Shapes.empty();
        }
        return super.getShape(state, level, pos, context);
    }

    private boolean isGhostFor(Player player) {
        return player.getCapability(SanityProvider.SANITY_CAP)
                .map(sanity -> ghostStages.contains(sanity.getStage()))
                .orElse(false);
    }

    public static final BooleanProperty GHOST = BooleanProperty.create("ghost");

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        System.out.println("animateTick called, GHOST=" + state.getValue(GHOST));
        if (!state.getValue(GHOST)) return;

        for (int i = 0; i < 3; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();

            level.addParticle(
                    ParticleTypes.SMOKE,
                    x, y, z,
                    0, 0.02, 0
            );
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(GHOST);
    }
}