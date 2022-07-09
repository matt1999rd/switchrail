package fr.mattmouss.switchrail.switchblock;

import fr.mattmouss.switchrail.enum_rail.Corners;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public abstract class Switch extends AbstractRailBlock {

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
    
    public BlockState getBlockState(World world, BlockPos pos){
        return world.getBlockState(pos);
    }

    public void updatePoweredState(World world, BlockState state, BlockPos pos, PlayerEntity player, int flags, boolean fromScreen){
        if (!world.isClientSide || fromScreen){
            RailShape currentRailShape = world.getBlockState(pos).getValue(RAIL_STRAIGHT_FLAT);
            world.setBlock(pos,state.cycle(getSwitchPositionProperty()).setValue(RAIL_STRAIGHT_FLAT,currentRailShape),flags);
        }
    }

    public abstract EnumProperty<Corners> getSwitchPositionProperty();

    @Override
    public boolean is(@Nonnull ITag<Block> tag) {
        return (tag == BlockTags.RAILS);
    }


    @Nonnull
    @Override
    protected BlockState updateDir(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, boolean placing) {
        System.out.println("getUpdatedState called");
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.ENABLED);
    }
}
