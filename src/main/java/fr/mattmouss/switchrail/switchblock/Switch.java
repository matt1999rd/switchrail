package fr.mattmouss.switchrail.switchblock;

import fr.mattmouss.switchrail.blocks.IAxleCounterDetector;
import fr.mattmouss.switchrail.enum_rail.Corners;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;


public abstract class Switch extends BaseRailBlock implements IAxleCounterDetector {

    public static EnumProperty<RailShape> RAIL_STRAIGHT_FLAT;
    public static EnumProperty<Corners>
            SWITCH_POSITION_STANDARD,
            Y_SWITCH_POSITION,
            THREE_WAY_SWITCH_POSITION,DSS_POSITION;

    static {
        RAIL_STRAIGHT_FLAT = EnumProperty.create("shape",RailShape.class,(railShape -> (railShape == RailShape.NORTH_SOUTH || railShape == RailShape.EAST_WEST)));
        SWITCH_POSITION_STANDARD = EnumProperty.create("switch_position", Corners.class, Corners.STRAIGHT, Corners.TURN);
        Y_SWITCH_POSITION = EnumProperty.create("switch_position",Corners.class, Corners.TURN_LEFT, Corners.TURN_RIGHT );
        THREE_WAY_SWITCH_POSITION = EnumProperty.create("switch_position",Corners.class,(corners -> (corners != Corners.TURN)));
        DSS_POSITION = EnumProperty.create("dss_active_rail",Corners.class);
    }

    @Nonnull
    @Override
    public Property<RailShape> getShapeProperty() {
        return RAIL_STRAIGHT_FLAT;
    }

    protected Switch(Properties p_i48444_2_) {
        super(true, p_i48444_2_);
        this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.ENABLED,true));
    }
    
    public BlockState getBlockState(Level world, BlockPos pos){
        return world.getBlockState(pos);
    }

    public void updatePoweredState(Level world, BlockState state, BlockPos pos, Player player, int flags, boolean fromScreen){
        if (!world.isClientSide || fromScreen){
            RailShape currentRailShape = world.getBlockState(pos).getValue(RAIL_STRAIGHT_FLAT);
            world.setBlock(pos,state.cycle(getSwitchPositionProperty()).setValue(RAIL_STRAIGHT_FLAT,currentRailShape),flags);
        }
    }

    public abstract EnumProperty<Corners> getSwitchPositionProperty();

    @Nonnull
    @Override
    protected BlockState updateDir(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, boolean placing) {
        System.out.println("getUpdatedState called");
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.ENABLED,WATERLOGGED);
    }

    @Override
    public void onMinecartPass(BlockState state, Level world, BlockPos pos, AbstractMinecart cart) {
        RailShape shape = this.getRailDirection(state,world,pos,cart);
        onMinecartPass(world,pos,cart,shape);
        super.onMinecartPass(state, world, pos, cart);
    }

    @Override
    public void onRemove(BlockState oldState, Level world, BlockPos pos, BlockState actualState, boolean p_196243_5_) {
        removeCP(world,oldState,actualState,pos);
        super.onRemove(oldState, world, pos, actualState, p_196243_5_);
    }
}
