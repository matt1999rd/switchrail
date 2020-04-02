package fr.mattmouss.switchrail.item;

import fr.mattmouss.switchrail.blocks.ControllerBlock;
import fr.mattmouss.switchrail.blocks.ControllerTile;
import fr.mattmouss.switchrail.blocks.SwitchTile;
import fr.mattmouss.switchrail.switchblock.Switch;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SwitchRegister extends Item {

    SwitchTile switch_tile_register ;

    public SwitchRegister(Properties properties) {
        super(properties.maxStackSize(1));
        this.setRegistryName("switch_register");
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        BlockPos pos=context.getPos();

        World world = context.getWorld();

        PlayerEntity player= context.getPlayer();

        TileEntity clickedBlockTile = world.getTileEntity(pos);
        if (clickedBlockTile != null) {
            if (clickedBlockTile.getBlockState().getBlock() instanceof Switch && !world.isRemote) {
                switch_tile_register = (SwitchTile) clickedBlockTile;
                System.out.println("------------Successfully register SwitchBlock :" + clickedBlockTile.getBlockState().getBlock().toString() + "--------");
                return ActionResultType.SUCCESS;
            } else if (clickedBlockTile.getBlockState().getBlock() instanceof ControllerBlock && !world.isRemote && switch_tile_register != null) {
                ControllerTile te = (ControllerTile) world.getTileEntity(pos);
                te.addSwitch(switch_tile_register);
                System.out.println("-------------Successfully add switch to Controller--------------------");
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.FAIL;
    }
}
