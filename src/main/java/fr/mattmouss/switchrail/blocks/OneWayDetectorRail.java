package fr.mattmouss.switchrail.blocks;

import fr.mattmouss.switchrail.other.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DetectorRailBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

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
    public boolean is(ITag<Block> tag) {
        return (tag == BlockTags.RAILS);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FIRST_WAY_DET);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
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
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClientSide && entity instanceof AbstractMinecartEntity){
            AbstractMinecartEntity minecart = (AbstractMinecartEntity) entity;
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
