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
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class SwitchSimpleSlip extends Switch  {

    private RailShape fixedRailShape =RailShape.NORTH_SOUTH;

    public SwitchSimpleSlip() {
        super(Properties.of(Material.METAL)
                .noCollission()
                .lightLevel(state -> 0)
                .strength(2f)
                .sound(SoundType.METAL));
        setRegistryName("switch_tjs");
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(SWITCH_POSITION_STANDARD, Corners.STRAIGHT)
                .setValue(RAIL_STRAIGHT_FLAT,RailShape.NORTH_SOUTH)
        );
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity !=null) {

            world.setBlockAndUpdate(pos,state
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Util.getFacingFromEntity(entity, pos,true))
                    .setValue(SWITCH_POSITION_STANDARD,Corners.STRAIGHT)
                    .setValue(RAIL_STRAIGHT_FLAT,RailShape.NORTH_SOUTH)
            );
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING,
                SWITCH_POSITION_STANDARD,RAIL_STRAIGHT_FLAT);
        super.createBlockStateDefinition(builder);
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
    public RailShape getRailDirection(BlockState state, IBlockReader reader, BlockPos pos, @Nullable AbstractMinecartEntity entity) {
        if (entity != null){
            RailShape railShape = getRailShapeFromEntityAndState(pos,entity,state);
            if (!minecartArrivedOnBlock(entity,pos)){
                fixedRailShape=railShape;
            }
        }
        return fixedRailShape;

    }

    private boolean minecartArrivedOnBlock(AbstractMinecartEntity entity, BlockPos pos) {
        return (entity.getCommandSenderWorld().isClientSide ||
                entity.xo > pos.getX() &&
                        entity.xo < pos.getX()+1 &&
                        entity.zo > pos.getZ() &&
                        entity.zo < pos.getZ()+1
        );

    }
    public RailShape getRailShapeFromEntityAndState(BlockPos pos, AbstractMinecartEntity entity,BlockState state) {
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Corners actualState = state.getValue(SWITCH_POSITION_STANDARD);
        if (entity.xo<pos.getX() || entity.xo> pos.getX()+1) {
            switch (direction) {
                case NORTH:
                    return (actualState == Corners.TURN) ? RailShape.SOUTH_WEST : RailShape.EAST_WEST;
                case SOUTH:
                    return (actualState == Corners.TURN) ? RailShape.NORTH_EAST : RailShape.EAST_WEST;
                case WEST:
                    return (actualState == Corners.TURN) ? RailShape.SOUTH_EAST : RailShape.EAST_WEST;
                case EAST:
                    return (actualState == Corners.TURN) ? RailShape.NORTH_WEST : RailShape.EAST_WEST;
                default:
                    throw new IllegalArgumentException("no such direction for tjs block");
            }
        } else {
            switch (direction) {
                case NORTH:
                    return (actualState == Corners.TURN) ? RailShape.SOUTH_WEST : RailShape.NORTH_SOUTH;
                case SOUTH:
                    return (actualState == Corners.TURN) ? RailShape.NORTH_EAST : RailShape.NORTH_SOUTH;
                case WEST:
                    return (actualState == Corners.TURN) ? RailShape.SOUTH_EAST : RailShape.NORTH_SOUTH;
                case EAST:
                    return (actualState == Corners.TURN) ? RailShape.NORTH_WEST : RailShape.NORTH_SOUTH;
                default:
                    throw new IllegalArgumentException("no such direction for tjs block");
            }
        }
        //  ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH;
    }

}
