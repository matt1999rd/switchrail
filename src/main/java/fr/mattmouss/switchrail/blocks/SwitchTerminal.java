package fr.mattmouss.switchrail.blocks;

import fr.mattmouss.switchrail.network.Networking;
import fr.mattmouss.switchrail.network.OpenScreenPacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
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

import javax.annotation.Nullable;

public class SwitchTerminal extends Block {

    public static final BooleanProperty IS_BLOCKED = BooleanProperty.create("is_blocked");
    public SwitchTerminal() {
        super(Properties.of(Material.METAL).strength(2.0F).noOcclusion());
        setRegistryName("switch_terminal");
        this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.POWERED, Boolean.FALSE).setValue(IS_BLOCKED,false));
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        return VoxelShapes.or(
                Block.box(0,0,0,16,1,16), //bottom part
                Block.box(7,0,7,9,16,9), //vertical part
                Block.box(6,12,6,10,17,10)); //top redstone part
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext blockItemUseContext) {
        return this.defaultBlockState().setValue(BlockStateProperties.POWERED, blockItemUseContext.getLevel().hasNeighborSignal(blockItemUseContext.getClickedPos()));
    }

    @Override
    public void onPlace(BlockState state, World world, BlockPos pos, BlockState p_220082_4_, boolean b) {
        for (Direction direction : Direction.values()) {
            world.updateNeighborsAt(pos.relative(direction), this);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED,IS_BLOCKED);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TerminalTile();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.DOWN && !facingState.getMaterial().blocksMotion()){
            if (worldIn instanceof World){
                dropResources(stateIn,(World) worldIn,currentPos);
            }
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos pos1, boolean bd) {
        world.setBlock(pos, state.setValue(BlockStateProperties.POWERED,world.hasNeighborSignal(pos)), 2);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if (!world.isClientSide){
            //send a packet to client to open screen
            Networking.INSTANCE.sendTo(new OpenScreenPacket(pos,false),((ServerPlayerEntity) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return side != null;
    }
}
