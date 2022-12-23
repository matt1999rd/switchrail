package fr.mattmouss.switchrail.switchblock;

import fr.mattmouss.switchrail.enum_rail.Corners;
import fr.mattmouss.switchrail.other.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class SwitchDoubleTurn extends Switch {


    public SwitchDoubleTurn() {
        super(Properties.of(Material.METAL)
        .noCollission()
        .lightLevel(state -> 0)
        .strength(2f)
        .sound(SoundType.METAL));
        setRegistryName("double_turn_switch");
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.NORTH)
                .setValue(Y_SWITCH_POSITION,Corners.TURN_LEFT)
        );

    }




    @Override
    public boolean canMakeSlopes(BlockState p_canMakeSlopes_1_, IBlockReader p_canMakeSlopes_2_, BlockPos p_canMakeSlopes_3_) {
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return true;
    }




    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity !=null) {
            world.setBlockAndUpdate(pos,state
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Util.getFacingFromEntity(entity, pos,true))
                    .setValue(Y_SWITCH_POSITION,Corners.TURN_LEFT)
                    .setValue(RAIL_STRAIGHT_FLAT,RailShape.NORTH_SOUTH)
            );
        }
    }


    @Override
    public void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> builder){
        builder.add(BlockStateProperties.HORIZONTAL_FACING,
                Y_SWITCH_POSITION,
                RAIL_STRAIGHT_FLAT);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public EnumProperty<Corners> getSwitchPositionProperty() {
        return Y_SWITCH_POSITION;
    }


    @Override
    public RailShape getRailDirection(BlockState state, IBlockReader reader, BlockPos pos, @Nullable AbstractMinecartEntity minecartEntity) {
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Corners actualState = state.getValue(Y_SWITCH_POSITION);
        return Util.getShapeFromDirection(direction.getOpposite(),actualState.getHeelDirection(direction.getOpposite()));
    }

    @Nonnull
    @Override
    public Property<RailShape> getShapeProperty() {
        return RAIL_STRAIGHT_FLAT;
    }



}
