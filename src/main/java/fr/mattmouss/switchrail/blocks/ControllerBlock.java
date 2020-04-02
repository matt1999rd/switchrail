package fr.mattmouss.switchrail.blocks;


import net.minecraft.block.*;
import net.minecraft.block.material.Material;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import org.lwjgl.opengl.ARBTessellationShader;

import javax.annotation.Nullable;

public class ControllerBlock extends Block {

    public ControllerBlock() {
        super(Properties.create(Material.ROCK)
                .hardnessAndResistance(2f)
                .sound(SoundType.METAL)
                .lightValue(0)
                .notSolid()
        );
        this.setRegistryName("controller_block");
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ControllerTile();
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {

        if (placer != null){
            worldIn.setBlockState(pos,state.with(BlockStateProperties.HORIZONTAL_FACING,getDirectionFromEntity(placer,pos)));
        }
    }




    private Direction getDirectionFromEntity(LivingEntity placer, BlockPos pos) {
        Vec3d vec = placer.getPositionVec();
        Direction d = Direction.getFacingFromVector(vec.x-pos.getX(),vec.y-pos.getY(),vec.z-pos.getZ());
        if (d==Direction.DOWN || d==Direction.UP){
            return Direction.NORTH;
        }
        return d;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }


    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        ControllerTile te = (ControllerTile) worldIn.getTileEntity(pos);
        System.out.println("openning gui !!");
        if (!worldIn.isRemote){
            NetworkHooks.openGui((ServerPlayerEntity)player,te,te.getPos());
        }
        return ActionResultType.SUCCESS;
    }





}
