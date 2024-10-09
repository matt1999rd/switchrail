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
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SwitchTriple extends Switch {

    public SwitchTriple() {
        super(Properties.of(Material.METAL)
                .noCollission()
                .lightLevel(state -> 0)
                .strength(2f)
                .sound(SoundType.METAL));
        this.setRegistryName("triple_switch");
        this.registerDefaultState(getChangedBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(THREE_WAY_SWITCH_POSITION,Corners.TURN_LEFT)
                .setValue(RAIL_STRAIGHT_FLAT,RailShape.NORTH_SOUTH)
        );
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity !=null) {
            world.setBlockAndUpdate(pos,state
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Util.getFacingFromEntity(entity, pos,true))
                    .setValue(THREE_WAY_SWITCH_POSITION,Corners.TURN_LEFT)
                    .setValue(RAIL_STRAIGHT_FLAT,RailShape.NORTH_SOUTH)
            );

        }
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(BlockStateProperties.HORIZONTAL_FACING,
                THREE_WAY_SWITCH_POSITION,RAIL_STRAIGHT_FLAT);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public RailShape getRailDirection(BlockState state, BlockGetter reader, BlockPos pos, @Nullable AbstractMinecart entity) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Corners swPosition = state.getValue(THREE_WAY_SWITCH_POSITION);
        Direction trailingDirection = swPosition.getHeelDirection(facing.getOpposite());
        return Util.getShapeFromDirection(facing.getOpposite(),trailingDirection);
    }


    @Nonnull
    @Override
    public Property<RailShape> getShapeProperty() {
        return RAIL_STRAIGHT_FLAT;
    }

    @Override
    public EnumProperty<Corners> getSwitchPositionProperty() {
        return Switch.THREE_WAY_SWITCH_POSITION;
    }
}
