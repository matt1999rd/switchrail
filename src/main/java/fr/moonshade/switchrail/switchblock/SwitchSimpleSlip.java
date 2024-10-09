package fr.moonshade.switchrail.switchblock;

import fr.moonshade.switchrail.blocks.ICrossedRail;
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
//import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
//import net.minecraft.world.gen.feature.structure.MineshaftPieces;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SwitchSimpleSlip extends Switch implements ICrossedRail {

    private RailShape fixedRailShape =RailShape.NORTH_SOUTH;

    //Position of the switch from the facing direction :
    // NORTH :
    //             |
    //          W ---
    //            \|
    //             S
    // EAST :      N
    //            /|
    //          W ---
    //             |
    // SOUTH :     N
    //             |\
    //            --- E
    //             |
    // WEST :
    //             |
    //            --- E
    //             |/
    //             S
    // Switch in the quadrant dir.getOpposite and dir.getCCW

    public SwitchSimpleSlip() {
        super(Properties.of(Material.METAL)
                .noCollission()
                .lightLevel(state -> 0)
                .strength(2f)
                .sound(SoundType.METAL));
        setRegistryName("switch_tjs");
        this.registerDefaultState(getChangedBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(SWITCH_POSITION_STANDARD, Corners.STRAIGHT)
                .setValue(RAIL_STRAIGHT_FLAT,RailShape.NORTH_SOUTH)
        );
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity !=null) {

            world.setBlockAndUpdate(pos,state
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Util.getFacingFromEntity(entity, pos,true))
                    .setValue(SWITCH_POSITION_STANDARD,Corners.STRAIGHT)
                    .setValue(RAIL_STRAIGHT_FLAT,RailShape.NORTH_SOUTH)
            );
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
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
    public RailShape getRailDirection(BlockState state, BlockGetter reader, BlockPos pos, @Nullable AbstractMinecart entity) {
        if (entity != null){
            RailShape railShape = getRailShapeFromEntityAndState(pos,entity,state);
            if (!minecartArrivedOnBlock(entity,pos)){
                fixedRailShape=railShape;
            }
        }
        return fixedRailShape;

    }

    private boolean minecartArrivedOnBlock(AbstractMinecart entity, BlockPos pos) {
        return (entity.getCommandSenderWorld().isClientSide ||
                entity.xo > pos.getX() &&
                        entity.xo < pos.getX()+1 &&
                        entity.zo > pos.getZ() &&
                        entity.zo < pos.getZ()+1
        );
    }


    public RailShape getRailShapeFromEntityAndState(BlockPos pos, AbstractMinecart entity,BlockState state) {
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Corners actualState = state.getValue(SWITCH_POSITION_STANDARD);
        if (actualState == Corners.STRAIGHT){
            return ICrossedRail.super.getRailShapeFromEntityAndState(pos,entity);
        }else {
            // look at first comment to understand the two directions
            return Util.getShapeFromDirection(direction.getOpposite(),direction.getCounterClockWise());
        }
    }

}
