package gravity_changer.block;

//import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
////import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
//import gravity_changer.GravityChangerBlocks;
import gravity_changer.block_entity.GravityPlatingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
//import net.minecraft.core.Registry;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.resources.Identifier;
//import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.context.BlockPlaceContext;
//import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
//import net.minecraft.world.level.LevelAccessor;
//import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.level.block.Mirror;
//import net.minecraft.world.level.block.RenderShape;
//import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.level.block.entity.BlockEntityTicker;
//import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
//import net.minecraft.world.phys.shapes.CollisionContext;
//import net.minecraft.world.phys.shapes.Shapes;
//import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class GravityPlatingBlock extends BaseEntityBlock {
    public static final MapCodec<GravityPlatingBlock> CODEC = simpleCodec(GravityPlatingBlock::new);

    // in a corner, multiple faces of plates can occupy the same block
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    public static BooleanProperty directionToProperty(Direction direction) {
        return switch (direction) {
            case DOWN -> DOWN;
            case UP -> UP;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
        };
    }

    public GravityPlatingBlock(Properties settings) {
        super(settings);

        registerDefaultState(defaultBlockState()
            .setValue(NORTH, false)
            .setValue(EAST, false)
            .setValue(SOUTH, false)
            .setValue(WEST, false)
            .setValue(UP, false)
            .setValue(DOWN, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<@NotNull Block, @NotNull BlockState> stateManager) {
        stateManager.add(UP, DOWN, NORTH, SOUTH, EAST, WEST);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos position, @NotNull BlockState state) {
        return new GravityPlatingBlockEntity(position, state);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(
            @NotNull BlockState blockState,
            @NotNull Level level,
            @NotNull BlockPos blockPosition,
            @NotNull Player player,
            @NotNull BlockHitResult blockHitResult
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Direction hitDir = blockHitResult.getDirection();
        Direction plateDir = hitDir.getOpposite();

        BlockEntity blockEntity = level.getBlockEntity(blockPosition);

        if (!(blockEntity instanceof GravityPlatingBlockEntity be)) {
            return InteractionResult.FAIL;
        }

        return be.interact(level, blockPosition, plateDir, player/*, blockHitResult*/);
    }

    /**
     * Similar to {@link ShulkerBoxBlock#playerWillDestroy(Level, BlockPos, BlockState, Player)}
     * Make it drop in creative mode.
     *
     */
    @Override
    public @NotNull BlockState playerWillDestroy(
            Level level,
            @NotNull BlockPos blockPosition,
            @NotNull BlockState state,
            @NotNull Player player
    ) {
        BlockEntity blockEntity = level.getBlockEntity(blockPosition);
        if (blockEntity instanceof GravityPlatingBlockEntity be &&
                !level.isClientSide() && player.isCreative()
        ) {
            List<ItemStack> drops = be.getDrops();

            for (ItemStack itemStack : drops) {
                ItemEntity itemEntity = new ItemEntity(
                        level,
                        (double) blockPosition.getX() + 0.5,
                        (double) blockPosition.getY() + 0.5,
                        (double) blockPosition.getZ() + 0.5,
                        itemStack
                );
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }
        }

        return super.playerWillDestroy(level, blockPosition, state, player);
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState blockState, LootParams.Builder builder) {
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof GravityPlatingBlockEntity be) {
            return be.getDrops();
        }

        return List.of();
    }

//    protected static final VoxelShape DOWN_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
//    protected static final VoxelShape UP_SHAPE = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
//    protected static final VoxelShape NORTH_SHAPE = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
//    protected static final VoxelShape SOUTH_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
//    protected static final VoxelShape WEST_SHAPE = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
//    protected static final VoxelShape EAST_SHAPE = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
//    private final Map<BlockState, VoxelShape> shapesByState;
//        this.shapesByState =
//            ImmutableMap.copyOf(
//                this.stateDefinition.getPossibleStates().stream()
//                    .collect(Collectors.toMap(Function.identity(), GravityPlatingBlock::getShapeForState))
//            );
//
//    private static VoxelShape getShapeForState(BlockState state) {
//        VoxelShape voxelShape = Shapes.empty();
//        if (state.getValue(UP)) {
//            voxelShape = UP_SHAPE;
//        }
//        if (state.getValue(NORTH)) {
//            voxelShape = Shapes.or(voxelShape, SOUTH_SHAPE);
//        }
//        if (state.getValue(SOUTH)) {
//            voxelShape = Shapes.or(voxelShape, NORTH_SHAPE);
//        }
//        if (state.getValue(EAST)) {
//            voxelShape = Shapes.or(voxelShape, WEST_SHAPE);
//        }
//        if (state.getValue(WEST)) {
//            voxelShape = Shapes.or(voxelShape, EAST_SHAPE);
//        }
//        if (state.getValue(DOWN)) {
//            voxelShape = Shapes.or(voxelShape, DOWN_SHAPE);
//        }
//        return voxelShape.isEmpty() ? Shapes.block() : voxelShape;
//    }
//
//    @Override
//    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
//        return this.shapesByState.get(state);
//    }



//    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
//        if (hasDir(state, direction) && !canPlaceOn(world, pos.relative(direction), direction.getOpposite())) {
//            state = state.setValue(directionToProperty(direction), false);
//            if (getDirections(state).size() == 0) {
//                return Blocks.AIR.defaultBlockState();
//            }
//            else {
//                return state;
//            }
//        }
//        else {
//            return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
//        }
//    }
//
//    @Override
//    public BlockState rotate(BlockState state, Rotation rotation) {
//        switch (rotation) {
//            case CLOCKWISE_180 -> {
//                return (((state.setValue(NORTH, state.getValue(SOUTH))).setValue(EAST, state.getValue(WEST))).setValue(SOUTH, state.getValue(NORTH))).setValue(WEST, state.getValue(EAST));
//            }
//            case COUNTERCLOCKWISE_90 -> {
//                return (((state.setValue(NORTH, state.getValue(EAST))).setValue(EAST, state.getValue(SOUTH))).setValue(SOUTH, state.getValue(WEST))).setValue(WEST, state.getValue(NORTH));
//            }
//            case CLOCKWISE_90 -> {
//                return (((state.setValue(NORTH, state.getValue(WEST))).setValue(EAST, state.getValue(NORTH))).setValue(SOUTH, state.getValue(EAST))).setValue(WEST, state.getValue(SOUTH));
//            }
//        }
//        return state;
//    }
//
//    @Override
//    public BlockState mirror(BlockState state, Mirror mirror) {
//        switch (mirror) {
//            case LEFT_RIGHT -> {
//                return (state.setValue(NORTH, state.getValue(SOUTH))).setValue(SOUTH, state.getValue(NORTH));
//            }
//            case FRONT_BACK -> {
//                return (state.setValue(EAST, state.getValue(WEST))).setValue(WEST, state.getValue(EAST));
//            }
//        }
//        return super.mirror(state, mirror);
//    }



//    @Override
//    public RenderShape getRenderShape(BlockState state) {
//        // With inheriting from BlockWithEntity this defaults to INVISIBLE, so we need to change that!
//        return RenderShape.MODEL;
//    }
//
//    @Override
//    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
//        if (world.isClientSide)
//            return createTickerHelper(type, GravityPlatingBlockEntity.TYPE, GravityPlatingBlockEntity::tick);
//        else
//            return createTickerHelper(type, GravityPlatingBlockEntity.TYPE, GravityPlatingBlockEntity::tick);
//    }
//
//    @Override
//    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
//        if (!context.isSecondaryUseActive() && context.getItemInHand().getItem() == this.asItem()) {
//            return !hasDir(state, context.getClickedFace().getOpposite());
//        }
//        return super.canBeReplaced(state, context);
//    }
//
//    @Override
//    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
//        BlockState blockState = ctx.getLevel().getBlockState(ctx.getClickedPos());
//        if (blockState.is(this)) {
//            return blockState.setValue(directionToProperty(ctx.getClickedFace().getOpposite()), true);
//        }
//        return defaultBlockState().setValue(directionToProperty(ctx.getClickedFace().getOpposite()), true);
//    }
//
//    private boolean canPlaceOn(BlockGetter world, BlockPos pos, Direction side) {
//        BlockState blockState = world.getBlockState(pos);
//        return blockState.isFaceSturdy(world, pos, side);
//    }
//
//    @Override
//    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
//        ArrayList<Direction> directions = getDirections(state);
//        if (directions.size() == 1) {
//            return canPlaceOn(world, pos.relative(directions.get(0)), directions.get(0).getOpposite());
//        }
//        //Placing inside an existing plating
//        if (directions.size() > 1) {
//            for (Direction dir : getDirections(world.getBlockState(pos))) {
//                directions.remove(dir);
//            }
//            return canPlaceOn(world, pos.relative(directions.get(0)), directions.get(0).getOpposite());
//        }
//        return false;
//    }



//    // Note: the direction is gravity field direction. the facing is the opposite
//    public static boolean hasDir(BlockState blockState, Direction dir) {
//        return blockState.getValue(directionToProperty(dir));
//    }
//
//    public static ArrayList<Direction> getDirections(BlockState blockState) {
//        ArrayList<Direction> list = new ArrayList<>();
//        //Iterate directions
//        for (int directionId = 0; directionId < 6; directionId++) {
//            //Convert ID to Direction
//            Direction direction = Direction.from3DDataValue(directionId);
//            //If the plate has this direction
//            if (hasDir(blockState, direction)) {
//                list.add(direction);
//            }
//        }
//        return list;
//    }
//

//    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
//    }

}
