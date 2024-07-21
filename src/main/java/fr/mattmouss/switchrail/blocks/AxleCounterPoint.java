package fr.mattmouss.switchrail.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AxleCounterPoint extends Block implements ICounterPoint, EntityBlock {
    public AxleCounterPoint() {
        super(Properties.of(Material.METAL).strength(2.0F).noOcclusion());
        setRegistryName("axle_counter_point");
        this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.POWERED,true)); // for future blockstate
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new AxleCounterTile(blockPos,blockState);
    }

    @Override
    public void onRemove(BlockState oldState, @Nonnull Level world, @Nonnull BlockPos pos, BlockState actualState, boolean p_196243_5_) {
        // this function is done only server side
        if (actualState.getBlock() != oldState.getBlock()) {
            this.onACRemove(world,pos,-1);
            super.onRemove(oldState, world, pos, actualState, p_196243_5_);
        }
    }

    @Override
    public boolean isSignalSource(@Nonnull BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, @Nonnull BlockGetter reader, @Nonnull BlockPos pos, @Nonnull Direction direction) {
        if (state.getValue(BlockStateProperties.POWERED)){
            return 15;
        }else {
            return 0;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED);
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        return Shapes.or(
                Block.box(0,0,0,16,1,16), //bottom part
                Block.box(7,2,7,9,4,9)); //vertical part
    }

    // this function remove the block if the block below it is not a solid block
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.DOWN && !facingState.getMaterial().blocksMotion()){
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    // open the GUI when block is clicked

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        if (!world.isClientSide){
            this.onBlockClicked(world,pos, (ServerPlayer) player,-1);
        }
        return InteractionResult.SUCCESS;
    }

}
