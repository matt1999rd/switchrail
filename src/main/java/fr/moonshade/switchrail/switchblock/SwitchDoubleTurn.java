package fr.moonshade.switchrail.switchblock;

import fr.moonshade.switchrail.enum_rail.Corners;
import fr.moonshade.switchrail.other.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

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
        this.registerDefaultState(getChangedState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.NORTH)
                .setValue(Y_SWITCH_POSITION,Corners.TURN_LEFT)
        );

    }




    @Override
    public boolean canMakeSlopes(BlockState p_canMakeSlopes_1_, BlockGetter p_canMakeSlopes_2_, BlockPos p_canMakeSlopes_3_) {
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return true;
    }




    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity !=null) {
            world.setBlockAndUpdate(pos,state
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Util.getFacingFromEntity(entity, pos,true))
                    .setValue(Y_SWITCH_POSITION,Corners.TURN_LEFT)
                    .setValue(RAIL_STRAIGHT_FLAT,RailShape.NORTH_SOUTH)
            );
        }
    }


    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
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
    public RailShape getRailDirection(BlockState state, BlockGetter reader, BlockPos pos, @Nullable AbstractMinecart minecartEntity) {
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
