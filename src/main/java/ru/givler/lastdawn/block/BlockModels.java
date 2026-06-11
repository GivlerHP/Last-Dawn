package ru.givler.lastdawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import ru.givler.lastdawn.tile.ModelTileBase;

import javax.annotation.Nullable;

public class BlockModels extends HorizontalDirectionalBlock implements EntityBlock {

    // FACING унаследован из HorizontalDirectionalBlock (N/S/E/W)
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    // Признак верхней части для многоблочных моделей
    public static final BooleanProperty TOP = BooleanProperty.create("top");

    private final String textureName;
    private final String modelName;

    private boolean disableCollision = false;
    private int blockHeight = 1;

    // bounds[facing_index (0=N,1=S,2=W,3=E)][0..5] = minX,minY,minZ,maxX,maxY,maxZ
    @Nullable
    private VoxelShape[] rotationShapes = null;

    // Форма по умолчанию (половина блока по высоте, как было в 1.7.10)
    private VoxelShape defaultShape = Block.box(0, 0, 0, 16, 8, 16);

    public BlockModels(BlockBehaviour.Properties properties, String textureName, String modelName) {
        super(properties);
        this.textureName = textureName;
        this.modelName = modelName;
        // Регистрируем стейты по умолчанию
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(TOP, false)
        );
    }

    // ─── Builder-методы ──────────────────────────────────────────────────────

    public BlockModels setModelHeight(int height) {
        this.blockHeight = height;
        return this;
    }

    public BlockModels setCollisionEnabled(boolean enabled) {
        this.disableCollision = !enabled;
        return this;
    }

    /**
     * Задать форму хитбокса для каждого направления.
     * shapes[0] = NORTH, [1] = SOUTH, [2] = WEST, [3] = EAST
     * Каждый элемент — VoxelShape, созданный через Block.box(...)
     */
    public BlockModels setRotationShapes(VoxelShape[] shapes) {
        this.rotationShapes = shapes;
        return this;
    }

    /**
     * Задать форму по умолчанию (без поворота). Используется если rotationShapes == null.
     */
    public BlockModels setDefaultShape(VoxelShape shape) {
        this.defaultShape = shape;
        return this;
    }

    // ─── BlockState ──────────────────────────────────────────────────────────

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TOP);
    }

    // ─── Внешний вид ─────────────────────────────────────────────────────────

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // GeckoLib рендерит через BlockEntityRenderer — обычный рендер не нужен
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    // ─── Размещение ──────────────────────────────────────────────────────────

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        // Проверяем что все верхние блоки свободны
        for (int i = 1; i < blockHeight; i++) {
            BlockPos above = pos.above(i);
            if (!level.getBlockState(above).canBeReplaced(context)) {
                return null; // нельзя разместить
            }
        }

        Direction facing = context.getHorizontalDirection().getOpposite();
        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(TOP, false);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        // Расставляем верхние части
        Direction facing = state.getValue(FACING);
        for (int i = 1; i < blockHeight; i++) {
            BlockPos above = pos.above(i);
            level.setBlock(above,
                    defaultBlockState()
                            .setValue(FACING, facing)
                            .setValue(TOP, true),
                    3);
        }
    }

    // ─── Разрушение ──────────────────────────────────────────────────────────

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (state.getValue(TOP)) {
            // Верхняя часть — убираем низ
            BlockPos below = pos.below();
            if (level.getBlockState(below).is(this)) {
                level.setBlock(below,
                        net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 35);
            }
        } else {
            // Нижняя часть — убираем верхние
            for (int i = 1; i < blockHeight; i++) {
                BlockPos above = pos.above(i);
                if (level.getBlockState(above).is(this)) {
                    level.setBlock(above,
                            net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 35);
                }
            }
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // Верхняя часть не дропает предмет — дроп только из нижней
        if (state.getValue(TOP)) {
            // Убираем дроп — просто превращаем в воздух без лута
            level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 35);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    // ─── Коллизия и выделение ────────────────────────────────────────────────

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                               CollisionContext context) {
        return getShapeForState(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                        CollisionContext context) {
        if (disableCollision) return Shapes.empty();
        return getShapeForState(state);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty(); // не перекрывать соседей
    }

    private VoxelShape getShapeForState(BlockState state) {
        if (rotationShapes == null) return defaultShape;
        Direction facing = state.getValue(FACING);
        // NORTH=0, SOUTH=1, WEST=2, EAST=3
        int idx = switch (facing) {
            case NORTH -> 0;
            case SOUTH -> 1;
            case WEST  -> 2;
            case EAST  -> 3;
            default    -> 0;
        };
        if (idx < rotationShapes.length) return rotationShapes[idx];
        return defaultShape;
    }

    // ─── TileEntity (BlockEntity) ─────────────────────────────────────────────

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Верхняя часть не имеет BlockEntity
        if (state.getValue(TOP)) return null;
        return new ModelTileBase(pos, state, textureName, modelName);
    }

    // ─── Геттеры для item renderer ───────────────────────────────────────────

    public String getModelName()   { return modelName; }
    public String getTextureName() { return textureName; }
}