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

public class BumperTile extends TileEntity implements ITickableTileEntity,IRail {
    public BumperTile() {
        super(ModBlock.BUMPER_TILE);
    }

    @Override
    public void tick() {
        Direction dir = this.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING);
        changeRailNearBy(dir,this.world,this.pos);
        verifyBlockUnderneath();

    }

    public void verifyBlockUnderneath() {
        Block block_underneath = this.world.getBlockState(this.pos.down()).getBlock();
        if (block_underneath instanceof AirBlock){
            this.world.destroyBlock(this.pos,true);
            this.remove();
        }
    }

    public void  changeRailNearBy(Direction bumper_direction, World world, BlockPos bumper_pos){
        BlockPos connected_block_pos = bumper_pos.offset(bumper_direction);
        BlockState connected_block_state = world.getBlockState(connected_block_pos);
        if (connected_block_state.getBlock() instanceof AbstractRailBlock){
            RailShape railShape = (bumper_direction == Direction.EAST || bumper_direction == Direction.WEST) ? RailShape.EAST_WEST:RailShape.NORTH_SOUTH;
            if (connected_block_state.has(BlockStateProperties.RAIL_SHAPE)){
                world.setBlockState(connected_block_pos,connected_block_state.with(BlockStateProperties.RAIL_SHAPE, railShape));
            } else if (connected_block_state.has(BlockStateProperties.RAIL_SHAPE_STRAIGHT)){
                world.setBlockState(connected_block_pos,connected_block_state.with(BlockStateProperties.RAIL_SHAPE_STRAIGHT,railShape));
            }
        }
    }
}
