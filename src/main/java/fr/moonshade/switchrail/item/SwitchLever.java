package fr.moonshade.switchrail.item;

import fr.moonshade.switchrail.switchblock.Switch;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;



import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.Rarity;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;

import net.minecraft.world.level.Level;

public class SwitchLever extends Item {

    private static final Rarity rarity = Rarity.UNCOMMON;


    public SwitchLever(Properties builder) {
        super(builder.stacksTo(1).rarity(rarity));
        this.setRegistryName("switch_lever");
    }



    @Override
    public InteractionResult useOn(UseOnContext context) {

        BlockPos pos=context.getClickedPos();

        Level world = context.getLevel();

        Player player= context.getPlayer();

        Block clickedBlock = world.getBlockState(pos).getBlock();

        if ((clickedBlock instanceof Switch) && !world.isClientSide){
            Switch sw = (Switch)clickedBlock;
            BlockState state = sw.getBlockState(world,pos);
            if (state != null && state.getValue(BlockStateProperties.ENABLED)) {
                System.out.println("item lever use successfully");
                sw.updatePoweredState(world, state, pos,player,7,false);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;

        }


        return InteractionResult.FAIL;
    }

}
