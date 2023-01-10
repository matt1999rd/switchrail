package fr.mattmouss.switchrail.blocks;

import fr.mattmouss.switchrail.axle_point.CounterPointInfo;
import fr.mattmouss.switchrail.axle_point.WorldCounterPoints;
import fr.mattmouss.switchrail.network.Networking;
import fr.mattmouss.switchrail.network.OpenCounterScreenPacket;
import fr.mattmouss.switchrail.other.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AxleCounterPoint extends Block {
    public AxleCounterPoint() {
        super(Properties.of(Material.METAL).strength(2.0F).noOcclusion());
        setRegistryName("axle_counter_point");
        this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.POWERED,true)); // for future blockstate
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new AxleCounterTile();
    }

    @Override
    public void onRemove(BlockState oldState, @Nonnull World world, @Nonnull BlockPos pos, BlockState actualState, boolean p_196243_5_) {
        // this function is done only server side
        if (actualState.getBlock() != oldState.getBlock()) {
            WorldCounterPoints worldCP = Util.getWorldCounterPoint(world);
            worldCP.onACRemove(pos);
            super.onRemove(oldState, world, pos, actualState, p_196243_5_);
        }
    }

    @Override
    public boolean isSignalSource(@Nonnull BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, @Nonnull IBlockReader reader, @Nonnull BlockPos pos, @Nonnull Direction direction) {
        if (state.getValue(BlockStateProperties.POWERED)){
            return 15;
        }else {
            return 0;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        return VoxelShapes.or(
                Block.box(0,0,0,16,1,16), //bottom part
                Block.box(7,2,7,9,4,9)); //vertical part
    }

    // this function remove the block if the block below it is not a solid block
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.DOWN && !facingState.getMaterial().blocksMotion()){
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    // open the GUI when block is clicked

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if (!world.isClientSide){
            CounterPointInfo cpInfo = getCPInfo(world,pos.immutable());
            //send a packet to client to open screen
            Networking.INSTANCE.sendTo(new OpenCounterScreenPacket(pos, cpInfo),((ServerPlayerEntity) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
        return ActionResultType.SUCCESS;
    }

    private CounterPointInfo getCPInfo(World world, BlockPos acPos){
        WorldCounterPoints worldCP = Util.getWorldCounterPoint(world);
        return worldCP.getCPInfo(acPos);
    }
}
