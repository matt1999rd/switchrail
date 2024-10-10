package fr.moonshade.switchrail.switchblock;


import fr.moonshade.switchrail.enum_rail.Corners;
import fr.moonshade.switchrail.other.Util;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;

import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.state.properties.RailShape;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class SwitchStraight extends Switch {

    public SwitchStraight(){
        super(Properties.of(Material.METAL)
                .lightLevel(state -> 0)
                .strength(2f)
                .noCollission()
                .sound(SoundType.METAL));
        this.registerDefaultState(this.stateDefinition.any().setValue(RAIL_STRAIGHT_FLAT,RailShape.NORTH_SOUTH).setValue(SWITCH_POSITION_STANDARD,Corners.STRAIGHT));
        this.setRegistryName("switch_straight");
    }

    @Override
    public void setPlacedBy(World world , BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity !=null) {
            Direction facing = Util.getDirectionFromEntity(entity,pos);
            world.setBlockAndUpdate(pos,state
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                    .setValue(BlockStateProperties.DOOR_HINGE,Util.getHingeSideFromEntity(entity,pos,facing.getOpposite()))
            );
        }
    }

    @Override
    public void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> builder){
        builder.add(RAIL_STRAIGHT_FLAT,
                SWITCH_POSITION_STANDARD,
                BlockStateProperties.HORIZONTAL_FACING,
                BlockStateProperties.DOOR_HINGE);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public RailShape getRailDirection(BlockState state, IBlockReader world, BlockPos pos, @Nullable AbstractMinecartEntity cart) {
        Corners corners = state.getValue(SWITCH_POSITION_STANDARD);
        if (corners == Corners.STRAIGHT){
            Direction.Axis axis = state.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis();
            return (axis == Direction.Axis.X)? RailShape.EAST_WEST : RailShape.NORTH_SOUTH;
        }else {
            //direction of facing is opposite to the direction where the entity is looking
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            DoorHingeSide side = state.getValue(BlockStateProperties.DOOR_HINGE);
            Direction sideDirection = (side == DoorHingeSide.RIGHT)? facing.getClockWise() : facing.getCounterClockWise();
            return Util.getShapeFromDirection(facing.getOpposite(),sideDirection);
        }
    }

    @Override
    public boolean isFlexibleRail(BlockState p_isFlexibleRail_1_, IBlockReader p_isFlexibleRail_2_, BlockPos p_isFlexibleRail_3_) {
        return false;
    }

    @Nonnull
    @Override
    public Property<RailShape> getShapeProperty() {
        return RAIL_STRAIGHT_FLAT;
    }

    @Override
    public EnumProperty<Corners> getSwitchPositionProperty() {
        return SWITCH_POSITION_STANDARD;
    }

    @Override
    public boolean canMakeSlopes(BlockState p_canMakeSlopes_1_, IBlockReader p_canMakeSlopes_2_, BlockPos p_canMakeSlopes_3_) {
        return false;
    }


    @Nonnull
    @Override
    protected BlockState updateDir(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState state, boolean b) {
        return state;
    }
}