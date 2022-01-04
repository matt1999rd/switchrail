package fr.mattmouss.switchrail.blocks;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BumperTile extends TileEntity implements ITickableTileEntity {
    public BumperTile() {
        super(ModBlock.BUMPER_TILE);
    }

    @Override
    public void tick() {
        Direction dir = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        assert this.level != null;
        changeRailNearBy(dir,this.level,this.worldPosition);
    }

    public void changeRailNearBy(Direction bumper_direction, World world, BlockPos bumper_pos){
        BlockPos connected_block_pos = bumper_pos.relative(bumper_direction);
        BlockState connected_block_state = world.getBlockState(connected_block_pos);
        if (connected_block_state.getBlock() instanceof AbstractRailBlock){
            RailShape railShape = (bumper_direction.getAxis() == Direction.Axis.X) ? RailShape.EAST_WEST:RailShape.NORTH_SOUTH;
            if (connected_block_state.hasProperty(BlockStateProperties.RAIL_SHAPE)){
                world.setBlockAndUpdate(connected_block_pos,connected_block_state.setValue(BlockStateProperties.RAIL_SHAPE, railShape));
            } else if (connected_block_state.hasProperty(BlockStateProperties.RAIL_SHAPE_STRAIGHT)){
                world.setBlockAndUpdate(connected_block_pos,connected_block_state.setValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT,railShape));
            }
        }
    }
}
