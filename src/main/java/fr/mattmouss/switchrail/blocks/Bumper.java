package fr.mattmouss.switchrail.blocks;

import fr.mattmouss.switchrail.setup.VoxelInts;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;

import net.minecraft.entity.LivingEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
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
        super(Properties.create(Material.ROCK).lightValue(0).sound(SoundType.METAL).hardnessAndResistance(2f));
        setRegistryName("bumper");
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }



    @Override
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        Direction direction = state.get(BlockStateProperties.HORIZONTAL_FACING);
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
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null){
            Direction dir = getFacingFromEntity(placer,pos);
            worldIn.setBlockState(pos,state.with(BlockStateProperties.HORIZONTAL_FACING,dir));
            changeRailNearBy(dir,worldIn,pos);
        }

    }

    @Override
    public BlockRenderLayer func_180664_k() {
        return BlockRenderLayer.CUTOUT_MIPPED;
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


    private Direction getFacingFromEntity(LivingEntity placer, BlockPos pos) {
        Vec3d vec =placer.getPositionVec();
        Direction dir = Direction.getFacingFromVector(vec.x-pos.getX(),vec.y-pos.getY(),vec.z-pos.getZ());
        if (dir == Direction.UP || dir == Direction.DOWN){
            dir = Direction.NORTH;
        }
        return dir;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }


}
