package fr.moonshade.switchrail.blocks;

import fr.moonshade.switchrail.other.Util;
import fr.moonshade.switchrail.setup.VoxelInts;
//import net.minecraft.block.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;

import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockState;

public class Bumper extends Block implements EntityBlock {

    VoxelInts WOOD_1 = new VoxelInts(1,0,1,15,2,3);
    VoxelInts WOOD_2 = new VoxelInts(1,0,5,15,2,7);
    VoxelInts WOOD_3 = new VoxelInts(1,0,9,15,2,11);
    VoxelInts WOOD_4 = new VoxelInts(1,0,13,15,2,15);
    VoxelInts RAIL_1 = new VoxelInts(12,1,0,13,3,16);
    VoxelInts RAIL_2 = new VoxelInts(3,1,0,4,3,16);
    VoxelInts VERT_SUPPORT_1 = new VoxelInts(3,3,7,4,10,8);
    VoxelInts VERT_SUPPORT_2 = new VoxelInts(12,3,7,13,10,8);
    VoxelInts TAMPON_1 = new VoxelInts(11,6,6,14,9,7);
    VoxelInts TAMPON_2 = new VoxelInts(2,6,6,5,9,7);




    public Bumper() {
        super(Properties.of(Material.STONE).lightLevel(state -> 0).sound(SoundType.METAL).strength(2f).noOcclusion());
        setRegistryName("bumper");
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlock.BUMPER_TILE ? ((level1, blockPos, blockState, t) -> {
            if (t instanceof BumperTile){
                ((BumperTile) t).tick(level1,blockState,blockPos, (BumperTile) t);
            }
        }): null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return Shapes.or(
                WOOD_1.rotate(Direction.NORTH,direction).getAssociatedShape(),
                WOOD_2.rotate(Direction.NORTH,direction).getAssociatedShape(),
                WOOD_3.rotate(Direction.NORTH,direction).getAssociatedShape(),
                WOOD_4.rotate(Direction.NORTH,direction).getAssociatedShape(),
                RAIL_1.rotate(Direction.NORTH,direction).getAssociatedShape(),
                RAIL_2.rotate(Direction.NORTH,direction).getAssociatedShape(),
                VERT_SUPPORT_1.rotate(Direction.NORTH,direction).getAssociatedShape(),
                VERT_SUPPORT_2.rotate(Direction.NORTH,direction).getAssociatedShape(),
                TAMPON_1.rotate(Direction.NORTH,direction).getAssociatedShape(),
                TAMPON_2.rotate(Direction.NORTH,direction).getAssociatedShape());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BumperTile(blockPos, blockState);
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null){
            Direction dir = Util.getFacingFromEntity(placer,pos,false);
            worldIn.setBlockAndUpdate(pos,state.setValue(BlockStateProperties.HORIZONTAL_FACING,dir));
            changeRailNearBy(dir,worldIn,pos);
        }

    }



    public void  changeRailNearBy(Direction bumper_direction, Level world, BlockPos bumper_pos){
        BlockPos connected_block_pos = bumper_pos.relative(bumper_direction);
        BlockState connected_block_state = world.getBlockState(connected_block_pos);
        if (connected_block_state.getBlock() instanceof BaseRailBlock){
            RailShape railShape = (bumper_direction == Direction.EAST || bumper_direction == Direction.WEST) ? RailShape.EAST_WEST:RailShape.NORTH_SOUTH;
            if (connected_block_state.hasProperty(BlockStateProperties.RAIL_SHAPE)){
                world.setBlockAndUpdate(connected_block_pos,connected_block_state.setValue(BlockStateProperties.RAIL_SHAPE, railShape));
            } else if (connected_block_state.hasProperty(BlockStateProperties.RAIL_SHAPE_STRAIGHT)){
                world.setBlockAndUpdate(connected_block_pos,connected_block_state.setValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT,railShape));
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.DOWN && !facingState.getMaterial().blocksMotion()){
            if (worldIn instanceof Level){
                dropResources(stateIn,(Level) worldIn,currentPos);
            }
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

}
