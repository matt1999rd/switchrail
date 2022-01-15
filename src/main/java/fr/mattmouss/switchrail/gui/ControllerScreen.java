package fr.mattmouss.switchrail.gui;

import fr.mattmouss.switchrail.blocks.ControllerTile;
import fr.mattmouss.switchrail.blocks.IPosBaseTileEntity;
import fr.mattmouss.switchrail.enum_rail.Corners;
import fr.mattmouss.switchrail.enum_rail.RailType;
import fr.mattmouss.switchrail.network.ChangeSwitchPacket;
import fr.mattmouss.switchrail.network.Networking;
import fr.mattmouss.switchrail.other.Vector2i;
import fr.mattmouss.switchrail.switchblock.Switch;
import fr.mattmouss.switchrail.switchblock.SwitchDoubleSlip;
import fr.mattmouss.switchrail.switchdata.RailData;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;



public class ControllerScreen extends RailScreen implements IGuiEventListener {

    public ControllerScreen(BlockPos pos) {
        super(pos);
    }

    protected IPosBaseTileEntity getTileEntity(){
        assert this.minecraft != null;
        assert this.minecraft.level != null;
        TileEntity tileEntity = this.minecraft.level.getBlockEntity(pos);
        if (tileEntity instanceof ControllerTile){
            return (ControllerTile) tileEntity;
        }
        assert tileEntity != null;
        throw new IllegalStateException("BlockPos of the controller screen ("+pos+") is not associated with a correct tile entity (found tile entity of type "+tileEntity.getClass()+" ) !");
    }

    @Override
    public boolean onSwitchClicked(RailData data, double mouseX, double mouseY, Vector2i relative, int button) {
        if (isDisabled(data.pos) || button != 0)return false;
        ChangeSwitchPacket packet;
        assert this.minecraft != null;
        World world = this.minecraft.level;
        if (data.type == RailType.DOUBLE_SLIP) {
            //if it is tjd rail
            assert world != null;
            BlockState state = world.getBlockState(data.pos);
            Corners tjd_position = state.getValue(SwitchDoubleSlip.DSS_POSITION);
            SwitchDoubleSlip switch_tjd = (SwitchDoubleSlip)(state.getBlock());
            System.out.println("changing switch using gui successfully done !");
            // ld nearest is a boolean associated with the left down part in game -> left down part on screen is associated with the right up part in game and vice versa
            boolean ld_nearest = isRightUpNearestOnScreen(data.pos, relative, mouseX, mouseY);
            int flag = ld_nearest ? 0b11 : 0b01;
            switch_tjd.updatePowerState(world,state,data.pos,7,ld_nearest,tjd_position);
            packet = new ChangeSwitchPacket(data.pos, (byte) flag);
        } else {
            packet =new ChangeSwitchPacket(data.pos, (byte) 0);
            assert world != null;
            BlockState state = world.getBlockState(data.pos);
            Block block = state.getBlock();
            if (block instanceof Switch) {
                Switch sw = (Switch) block;
                System.out.println("changing switch using gui successfully done !");
                sw.updatePoweredState(world, state, data.pos, null, 7,true);
            }


        }
        Networking.INSTANCE.sendToServer(packet);
        return true;
    }

    @Override
    public boolean isDisabled(BlockPos pos) {
        assert this.minecraft != null;
        World world = this.minecraft.level;
        assert world != null;
        BlockState state = world.getBlockState(pos);
        if (state.hasProperty(BlockStateProperties.ENABLED)){
            return !state.getValue(BlockStateProperties.ENABLED);
        }
        return false;
    }

    public static void open(BlockPos pos){
        Minecraft.getInstance().setScreen(new ControllerScreen(pos));
    }

}

