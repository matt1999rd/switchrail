package fr.moonshade.switchrail.blocks;

import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BumperTile extends BlockEntity {
    public BumperTile(BlockPos blockPos, BlockState blockState) {
        super(ModBlock.BUMPER_TILE,blockPos,blockState);
    }

    public void tick(Level level, BlockState state,BlockPos pos, BumperTile tile) {
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        tile.changeRailNearBy(dir,level,pos);
    }

    public void changeRailNearBy(Direction bumper_direction, Level world, BlockPos bumper_pos){
        BlockPos connected_block_pos = bumper_pos.relative(bumper_direction);
        BlockState connected_block_state = world.getBlockState(connected_block_pos);
        if (connected_block_state.getBlock() instanceof BaseRailBlock){
            RailShape railShape = (bumper_direction.getAxis() == Direction.Axis.X) ? RailShape.EAST_WEST:RailShape.NORTH_SOUTH;
            if (connected_block_state.hasProperty(BlockStateProperties.RAIL_SHAPE)){
                world.setBlockAndUpdate(connected_block_pos,connected_block_state.setValue(BlockStateProperties.RAIL_SHAPE, railShape));
            } else if (connected_block_state.hasProperty(BlockStateProperties.RAIL_SHAPE_STRAIGHT)){
                world.setBlockAndUpdate(connected_block_pos,connected_block_state.setValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT,railShape));
            }
        }
    }

}
