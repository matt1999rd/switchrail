package fr.moonshade.switchrail.blocks;


import fr.moonshade.switchrail.network.Networking;
import fr.moonshade.switchrail.network.OpenControllerScreenPacket;
//import mcp.MethodsReturnNonnullByDefault;
//import net.minecraft.block.*;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.material.Material;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import fr.moonshade.switchrail.other.Util;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
//import net.minecraftforge.fml.network.NetworkDirection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;


import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class ControllerBlock extends Block implements EntityBlock {

    public ControllerBlock() {
        super(Properties.of(Material.STONE)
                .strength(2f)
                .sound(SoundType.METAL)
                .lightLevel(state -> 0)
                .noOcclusion()
        );
        this.setRegistryName("controller_block");
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ControllerTile(blockPos,blockState);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void setPlacedBy( Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null){
            worldIn.setBlockAndUpdate(pos,state.setValue(BlockStateProperties.HORIZONTAL_FACING,Util.getDirectionFromEntity(placer,pos,false)));
        }
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        System.out.println("opening gui !!");
        if (!worldIn.isClientSide){
            Networking.INSTANCE.sendTo(new OpenControllerScreenPacket(pos),((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }


}
