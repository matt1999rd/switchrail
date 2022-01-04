package fr.mattmouss.switchrail.switchblock;

import fr.mattmouss.switchrail.enum_rail.Corners;
import fr.mattmouss.switchrail.enum_rail.SwitchTypeOld;
import fr.mattmouss.switchrail.other.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

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
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(THREE_WAY_SWITCH_POSITION,Corners.TURN_LEFT)
                .setValue(RAIL_STRAIGHT_FLAT,RailShape.NORTH_SOUTH)
        );
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity !=null) {
            world.setBlockAndUpdate(pos,state
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Util.getFacingFromEntity(entity, pos,true))
                    .setValue(THREE_WAY_SWITCH_POSITION,Corners.TURN_LEFT)
                    .setValue(RAIL_STRAIGHT_FLAT,RailShape.NORTH_SOUTH)
            );

        }
    }

    @Override
    public void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> builder){
        builder.add(BlockStateProperties.HORIZONTAL_FACING,
                THREE_WAY_SWITCH_POSITION,RAIL_STRAIGHT_FLAT);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public RailShape getRailDirection(BlockState state, IBlockReader reader, BlockPos pos, @Nullable AbstractMinecartEntity entity) {
        switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)){
            case EAST:
                switch (state.getValue(THREE_WAY_SWITCH_POSITION)) {
                    case TURN_LEFT:
                        return RailShape.NORTH_WEST;
                    case STRAIGHT:
                        return RailShape.EAST_WEST;
                    case TURN_RIGHT:
                        return RailShape.SOUTH_WEST;
                    default:
                        throw new IllegalArgumentException("no such direction for triple switch block");
                }
            case WEST:
                switch (state.getValue(THREE_WAY_SWITCH_POSITION)) {
                    case TURN_LEFT:
                        return RailShape.SOUTH_EAST;
                    case STRAIGHT:
                        return RailShape.EAST_WEST;
                    case TURN_RIGHT:
                        return RailShape.NORTH_EAST;
                    default:
                        throw new IllegalArgumentException("no such direction for triple switch block");
                }
            case NORTH:
                switch (state.getValue(THREE_WAY_SWITCH_POSITION)) {
                    case TURN_LEFT:
                        return RailShape.SOUTH_WEST;
                    case STRAIGHT:
                        return RailShape.NORTH_SOUTH;
                    case TURN_RIGHT:
                        return RailShape.SOUTH_EAST;
                    default:
                        throw new IllegalArgumentException("no such direction for triple switch block");
                }
            case SOUTH:
                switch (state.getValue(THREE_WAY_SWITCH_POSITION)) {
                    case TURN_LEFT:
                        return RailShape.NORTH_WEST;
                    case STRAIGHT:
                        return RailShape.NORTH_SOUTH;
                    case TURN_RIGHT:
                        return RailShape.NORTH_EAST;
                    default:
                        throw new IllegalArgumentException("no such direction for triple switch block");
                }
            default:
                throw new IllegalArgumentException("no such direction for triple switch block");
        }
    }



    @Nonnull
    @Override
    public Property<RailShape> getShapeProperty() {
        return RAIL_STRAIGHT_FLAT;
    }

    @Override
    public EnumProperty<?> getSwitchPositionProperty() {
        return Switch.THREE_WAY_SWITCH_POSITION;
    }
}
