package fr.mattmouss.switchrail.blocks;

import fr.mattmouss.switchrail.enum_rail.ScreenType;
import fr.mattmouss.switchrail.network.ActionOnTilePacket;
import fr.mattmouss.switchrail.network.Networking;
import fr.mattmouss.switchrail.network.OpenScreenPacket;
import fr.mattmouss.switchrail.other.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
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
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.fml.network.NetworkDirection;

import javax.annotation.Nullable;
import java.util.List;

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

    //return the blockstate to configure when player is using the corresponding blockitem
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext blockItemUseContext) {
        BlockPos pos = blockItemUseContext.getClickedPos();
        LivingEntity placer = blockItemUseContext.getPlayer();
        Direction direction = Util.getDirectionFromEntity(placer,pos,true);
        return this.defaultBlockState().setValue(BlockStateProperties.POWERED, blockItemUseContext.getLevel()
                .getSignal(blockItemUseContext.getClickedPos(),getCommandDirection(direction)) > 0).setValue(BlockStateProperties.HORIZONTAL_FACING,direction);
    }

    // used for modification of redstone wire already placed nearby
    @Override
    public void onPlace(BlockState state, World world, BlockPos pos, BlockState p_220082_4_, boolean b) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        world.updateNeighborsAt(pos.relative(getCommandDirection(facing)),this);
        world.updateNeighborsAt(pos.relative(getControlDirection(facing)),this);
    }

    // it is a list of all blockstate properties
    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED,IS_BLOCKED,BlockStateProperties.HORIZONTAL_FACING);
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

    // this function remove the block if the block below it is not a solid block
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.DOWN && !facingState.getMaterial().blocksMotion()){
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    // used to power the terminal block if nearby block can power it
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos pos1, boolean bd) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        boolean wasPowered = state.getValue(BlockStateProperties.POWERED);
        BlockPos controlDirPos = pos.relative(getCommandDirection(facing));
        if (controlDirPos.equals(pos1)) {
            boolean isPowered = world.hasSignal(pos.relative(getCommandDirection(facing)), getCommandDirection(facing));
            world.setBlock(pos, state.setValue(BlockStateProperties.POWERED, isPowered), BlockFlags.DEFAULT);
            if (isPowered != wasPowered)actionOnPoweredBSPModification(world, pos, isPowered);
        }
    }

    // done when block is powered or unpowered
    // we need to block or free switch only if changes occurs in terminal : powering unpowered block or unpowering powered block

    public void actionOnPoweredBSPModification(World world,BlockPos pos,boolean isPowered){
        TerminalTile tile = (TerminalTile) world.getBlockEntity(pos);
        assert tile != null;
        //action done on tile entity : need packet matter to send to other client (search all player)
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
        } else {
            tile.actionOnUnpowered();
        }
    }

    // open the GUI when block is clicked

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if (!world.isClientSide){
            //send a packet to client to open screen
            Networking.INSTANCE.sendTo(new OpenScreenPacket(pos, ScreenType.TERMINAL),((ServerPlayerEntity) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public int getSignal(BlockState state, IBlockReader reader, BlockPos pos, Direction dir) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        boolean isBlocked = state.getValue(IS_BLOCKED);
        boolean isPowered = state.getValue(BlockStateProperties.POWERED);
        if (dir == getControlDirection(facing).getOpposite() && !isBlocked && isPowered){
            return 15;
        }
        return 0;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return side == getCommandDirection(facing) || side == getControlDirection(facing);
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

    public static Direction getCommandDirection(Direction facing){
        return facing.getClockWise();
    }

    public static Direction getControlDirection(Direction facing) { return facing.getCounterClockWise(); }
}
