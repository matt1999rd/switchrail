package fr.mattmouss.switchrail.blocks;


import fr.mattmouss.switchrail.network.Networking;
import fr.mattmouss.switchrail.network.OpenControllerScreenPacket;
import mcp.MethodsReturnNonnullByDefault;
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
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import fr.mattmouss.switchrail.other.Util;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;


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
    @ParametersAreNonnullByDefault
    public void setPlacedBy( World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null){
            worldIn.setBlockAndUpdate(pos,state.setValue(BlockStateProperties.HORIZONTAL_FACING,Util.getDirectionFromEntity(placer,pos,false)));
        }
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        System.out.println("opening gui !!");
        if (!worldIn.isClientSide){
            Networking.INSTANCE.sendTo(new OpenControllerScreenPacket(pos),((ServerPlayerEntity) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

}
