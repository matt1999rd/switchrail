package fr.mattmouss.switchrail.blocks;

import fr.mattmouss.switchrail.other.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class OneWayDetectorRail extends DetectorRailBlock {

    private static final BooleanProperty FIRST_WAY_DET;
    static {
        FIRST_WAY_DET = BooleanProperty.create("first_direction");
    }

    public OneWayDetectorRail() {
        super(Properties.of(Material.DECORATION).noCollission()
                .lightLevel(state -> 0)
                .strength(0.7f)
                .sound(SoundType.METAL));
        this.setRegistryName("one_way_detector_rail");
    }
    //todo : correct bug in rendering of ascending block and recipes loot tables

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FIRST_WAY_DET);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            Direction dir = Util.getDirectionFromEntity(placer,pos,false);
            RailShape shape = (dir.getAxis() == Direction.Axis.X) ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH;
            boolean firstWayDetected =
                    ((dir == Direction.SOUTH) && shape == RailShape.NORTH_SOUTH) ||
                            ((dir == Direction.WEST) && shape == RailShape.EAST_WEST);
            worldIn.setBlockAndUpdate(pos,state
                    .setValue(FIRST_WAY_DET, firstWayDetected)
                    .setValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT,shape));
        }
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (!world.isClientSide && entity instanceof AbstractMinecart){
            AbstractMinecart minecart = (AbstractMinecart) entity;
            Direction direction = minecart.getMotionDirection();
            RailShape shape = state.getValue(SHAPE);
            boolean firstWayDetected = state.getValue(FIRST_WAY_DET);
            if (Util.getRailShapeAxis(shape) == Direction.Axis.X && direction.getAxis() == Direction.Axis.X){
                if ((direction == Direction.EAST) == firstWayDetected) {
                    super.entityInside(state, world, pos, entity);
                }
            }else if (Util.getRailShapeAxis(shape) == Direction.Axis.Z && direction.getAxis() == Direction.Axis.Z){
                if ((direction == Direction.NORTH) == firstWayDetected){
                    super.entityInside(state, world, pos, entity);
                }
            }
        }
    }
}
