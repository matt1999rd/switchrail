package fr.mattmouss.switchrail.blocks;

import fr.mattmouss.switchrail.other.Util;
import fr.mattmouss.switchrail.setup.VoxelInts;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;

import net.minecraft.entity.LivingEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class Bumper extends Block {

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

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return VoxelShapes.or(
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
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new BumperTile();
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null){
            Direction dir = Util.getFacingFromEntity(placer,pos,false);
            worldIn.setBlockAndUpdate(pos,state.setValue(BlockStateProperties.HORIZONTAL_FACING,dir));
            changeRailNearBy(dir,worldIn,pos);
        }

    }



    public void  changeRailNearBy(Direction bumper_direction, World world, BlockPos bumper_pos){
        BlockPos connected_block_pos = bumper_pos.relative(bumper_direction);
        BlockState connected_block_state = world.getBlockState(connected_block_pos);
        if (connected_block_state.getBlock() instanceof AbstractRailBlock){
            RailShape railShape = (bumper_direction == Direction.EAST || bumper_direction == Direction.WEST) ? RailShape.EAST_WEST:RailShape.NORTH_SOUTH;
            if (connected_block_state.hasProperty(BlockStateProperties.RAIL_SHAPE)){
                world.setBlockAndUpdate(connected_block_pos,connected_block_state.setValue(BlockStateProperties.RAIL_SHAPE, railShape));
            } else if (connected_block_state.hasProperty(BlockStateProperties.RAIL_SHAPE_STRAIGHT)){
                world.setBlockAndUpdate(connected_block_pos,connected_block_state.setValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT,railShape));
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.DOWN && !facingState.getMaterial().blocksMotion()){
            if (worldIn instanceof World){
                dropResources(stateIn,(World) worldIn,currentPos);
            }
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

}
