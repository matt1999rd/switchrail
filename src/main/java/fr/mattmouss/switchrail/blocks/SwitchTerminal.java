package fr.mattmouss.switchrail.blocks;

import fr.mattmouss.switchrail.network.ActionOnTilePacket;
import fr.mattmouss.switchrail.network.Networking;
import fr.mattmouss.switchrail.network.OpenScreenPacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
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
import java.util.List;

public class SwitchTerminal extends Block {

    //todo : removing block is not releasing the switch bind to it
    
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
        boolean isPowered = world.hasNeighborSignal(pos);
        world.setBlock(pos, state.setValue(BlockStateProperties.POWERED,isPowered), 2);
        TerminalTile tile = (TerminalTile) world.getBlockEntity(pos);
        assert tile != null;
        List<? extends PlayerEntity> players = world.players();
        for (PlayerEntity player : players){
            if (player instanceof ServerPlayerEntity){
                Networking.INSTANCE.sendTo(new ActionOnTilePacket(pos,isPowered),((ServerPlayerEntity)player).connection.connection,NetworkDirection.PLAY_TO_CLIENT);
            }else {
                throw new IllegalStateException("Server player are mandatory here to push modification of switch enabled state to client ");
            }
        }
        if (isPowered){
            tile.actionOnPowered();
        }else {
            tile.actionOnUnpowered();
        }
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

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState futureState, boolean bool) {
        if (state.is(futureState.getBlock())) {
            super.onRemove(state, world, pos, futureState, bool);
            return;
        }
        TileEntity te = world.getBlockEntity(pos);
        if (te == null)throw new IllegalStateException("No tile entity found in position !");
        if (!state.getValue(IS_BLOCKED) && state.getValue(BlockStateProperties.POWERED)) {
            ((TerminalTile)te).freeAllSwitch();
        }
        super.onRemove(state, world, pos, futureState, bool);
    }

    @Override
    public void playerDestroy(World p_180657_1_, PlayerEntity p_180657_2_, BlockPos p_180657_3_, BlockState p_180657_4_, @Nullable TileEntity p_180657_5_, ItemStack p_180657_6_) {
        super.playerDestroy(p_180657_1_, p_180657_2_, p_180657_3_, p_180657_4_, p_180657_5_, p_180657_6_);
    }

    @Override
    public void playerWillDestroy(World p_176208_1_, BlockPos p_176208_2_, BlockState p_176208_3_, PlayerEntity p_176208_4_) {
        super.playerWillDestroy(p_176208_1_, p_176208_2_, p_176208_3_, p_176208_4_);
    }
}
