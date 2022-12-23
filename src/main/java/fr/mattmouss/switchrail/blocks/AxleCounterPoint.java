package fr.mattmouss.switchrail.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class AxleCounterPoint extends Block {
    public AxleCounterPoint(Properties properties) {
        super(properties);
        setRegistryName("axle_counter_point");
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return super.hasTileEntity(state);
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        return super.getShape(p_220053_1_, p_220053_2_, p_220053_3_, p_220053_4_);
    }
}
