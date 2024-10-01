package fr.moonshade.switchrail.switchblock;


import fr.moonshade.switchrail.enum_rail.Corners;
import fr.moonshade.switchrail.other.Util;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.RailShape;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

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
    public void setPlacedBy(Level world , BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity !=null) {
            Direction facing = Util.getDirectionFromEntity(entity,pos);
            world.setBlockAndUpdate(pos,state
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                    .setValue(BlockStateProperties.DOOR_HINGE,Util.getHingeSideFromEntity(entity,pos,facing.getOpposite()))
            );
        }
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(RAIL_STRAIGHT_FLAT,
                SWITCH_POSITION_STANDARD,
                BlockStateProperties.HORIZONTAL_FACING,
                BlockStateProperties.DOOR_HINGE);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public RailShape getRailDirection(BlockState state, BlockGetter world, BlockPos pos, @Nullable AbstractMinecart cart) {
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
    public boolean isFlexibleRail(BlockState p_isFlexibleRail_1_, BlockGetter p_isFlexibleRail_2_, BlockPos p_isFlexibleRail_3_) {
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
    public boolean canMakeSlopes(BlockState p_canMakeSlopes_1_, BlockGetter p_canMakeSlopes_2_, BlockPos p_canMakeSlopes_3_) {
        return false;
    }


    @Nonnull
    @Override
    protected BlockState updateDir(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState state, boolean b) {
        return state;
    }
}