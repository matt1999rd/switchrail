package fr.moonshade.switchrail.blocks;

import fr.moonshade.switchrail.network.ActionOnTilePacket;
import fr.moonshade.switchrail.network.Networking;
//import fr.mattmouss.switchrail.network.OpenControllerScreenPacket;
import fr.moonshade.switchrail.network.OpenTerminalScreenPacket;
import fr.moonshade.switchrail.other.Util;
//import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
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
import net.minecraftforge.fmllegacy.network.NetworkDirection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;


public class SwitchTerminal extends Block implements EntityBlock {
    
    public static final BooleanProperty IS_BLOCKED = BooleanProperty.create("is_blocked");
    public SwitchTerminal() {
        super(Properties.of(Material.METAL).strength(2.0F).noOcclusion());
        setRegistryName("switch_terminal");
        this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.POWERED, Boolean.FALSE).setValue(IS_BLOCKED,false));
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        return Shapes.or(
                Block.box(0,0,0,16,1,16), //bottom part
                Block.box(7,0,7,9,16,9), //vertical part
                Block.box(6,12,6,10,17,10)); //top redstone part
    }

    //return the blockstate to configure when player is using the corresponding blockitem
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockItemUseContext) {
        BlockPos pos = blockItemUseContext.getClickedPos();
        LivingEntity placer = blockItemUseContext.getPlayer();
        assert placer != null;
        Direction direction = Util.getDirectionFromEntity(placer,pos,true);
        return this.defaultBlockState().setValue(BlockStateProperties.POWERED, blockItemUseContext.getLevel()
                .getSignal(blockItemUseContext.getClickedPos(),getCommandDirection(direction)) > 0).setValue(BlockStateProperties.HORIZONTAL_FACING,direction);
    }

    // used for modification of redstone wire already placed nearby
    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, @Nonnull BlockState p_220082_4_, boolean b) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        world.updateNeighborsAt(pos.relative(getCommandDirection(facing)),this);
        world.updateNeighborsAt(pos.relative(getControlDirection(facing)),this);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlock.TERMINAL_TILE ? ((level1, blockPos, blockState, t) -> {
            if (t instanceof ITerminalHandler){
                ((ITerminalHandler) t).onTick();
            }
        }) : null;
    }

    // it is a list of all blockstate properties
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED,IS_BLOCKED,BlockStateProperties.HORIZONTAL_FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TerminalTile(blockPos,blockState);
    }

    // this function remove the block if the block below it is not a solid block
    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.DOWN && !facingState.getMaterial().blocksMotion()){
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    // used to power the terminal block if nearby block can power it
    @ParametersAreNonnullByDefault
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos pos1, boolean bd) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        boolean wasPowered = state.getValue(BlockStateProperties.POWERED);
        BlockPos controlDirPos = pos.relative(getCommandDirection(facing));
        if (controlDirPos.equals(pos1)) {
            boolean isPowered = world.hasSignal(pos.relative(getCommandDirection(facing)), getCommandDirection(facing));
            world.setBlock(pos, state.setValue(BlockStateProperties.POWERED, isPowered), 3);
            if (isPowered != wasPowered)actionOnPoweredBSPModification(world, pos, isPowered);
        }
    }

    // done when block is powered or unpowered
    // we need to block or free switch only if changes occurs in terminal : powering unpowered block or unpowering powered block

    public void actionOnPoweredBSPModification(Level world,BlockPos pos,boolean isPowered){
        TerminalTile tile = (TerminalTile) world.getBlockEntity(pos);
        assert tile != null;
        //action done on tile entity : need packet matter to send to other client (search all player)
        List<? extends Player> players = world.players();
        for (Player player : players){
            if (player instanceof ServerPlayer){
                Networking.INSTANCE.sendTo(new ActionOnTilePacket(pos,isPowered),((ServerPlayer)player).connection.connection,NetworkDirection.PLAY_TO_CLIENT);
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

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        if (!world.isClientSide){
            //send a packet to client to open screen
            Networking.INSTANCE.sendTo(new OpenTerminalScreenPacket(pos, -1),((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @ParametersAreNonnullByDefault
    public int getSignal(BlockState state, BlockGetter reader, BlockPos pos, Direction dir) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        boolean isBlocked = state.getValue(IS_BLOCKED);
        boolean isPowered = state.getValue(BlockStateProperties.POWERED);
        if (dir == getControlDirection(facing).getOpposite() && !isBlocked && isPowered){
            return 15;
        }
        return 0;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return side == getCommandDirection(facing) || side == getControlDirection(facing);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState futureState, boolean bool) {
        if (state.is(futureState.getBlock())) {
            super.onRemove(state, world, pos, futureState, bool);
            return;
        }
        BlockEntity te = world.getBlockEntity(pos);
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
