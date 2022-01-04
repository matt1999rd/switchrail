package fr.mattmouss.switchrail.blocks;


import fr.mattmouss.switchrail.network.Networking;
import fr.mattmouss.switchrail.network.OpenScreenPacket;
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
//import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;


public class ControllerBlock extends Block {

    public ControllerBlock() {
        super(Properties.of(Material.STONE)
                .strength(2f)
                .sound(SoundType.METAL)
                .lightLevel(state -> 0)
                .noOcclusion()
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
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {

        if (placer != null){
            worldIn.setBlockAndUpdate(pos,state.setValue(BlockStateProperties.HORIZONTAL_FACING,getDirectionFromEntity(placer,pos)));
        }
    }

/*
    //1.14.4 function replaced by .notSolid()
    @Override
    public BlockRenderLayer func_180664_k() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

 */






/*
    //1.14.4 function replaced by onBlockActivated
    @Override
    public boolean func_220051_a(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand p_220051_5_, BlockRayTraceResult p_220051_6_) {
        ControllerTile te = (ControllerTile) worldIn.getTileEntity(pos);
        System.out.println("opening gui !!");
        if (!worldIn.isRemote){
            NetworkHooks.openGui((ServerPlayerEntity)player,te,te.getPos());
        }
        return true;
    }
*/




    //1.15 function
    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        System.out.println("opening gui !!");
        if (!worldIn.isClientSide){
            Networking.INSTANCE.sendTo(new OpenScreenPacket(pos,true),((ServerPlayerEntity) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
        return ActionResultType.SUCCESS;
    }





    private Direction getDirectionFromEntity(LivingEntity placer, BlockPos pos) {
        Vector3d vec = placer.position();
        Direction d = Direction.getNearest(vec.x-pos.getX(),vec.y-pos.getY(),vec.z-pos.getZ());
        if (d==Direction.DOWN || d==Direction.UP){
            return Direction.NORTH;
        }
        return d;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

}
