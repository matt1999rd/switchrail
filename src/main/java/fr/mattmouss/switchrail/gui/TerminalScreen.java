package fr.mattmouss.switchrail.gui;

import fr.mattmouss.switchrail.blocks.IPosBaseTileEntity;
import fr.mattmouss.switchrail.blocks.TerminalTile;
import fr.mattmouss.switchrail.enum_rail.Dss_Position;
import fr.mattmouss.switchrail.enum_rail.RailType;
import fr.mattmouss.switchrail.network.Networking;
import fr.mattmouss.switchrail.network.TerminalScreenPacket;
import fr.mattmouss.switchrail.switchblock.Switch;
import fr.mattmouss.switchrail.switchdata.RailData;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector2f;

public class TerminalScreen extends RailScreen {
    public static final byte ADD_SWITCH_ACTION = 0;
    public static final byte REMOVE_SWITCH_ACTION = 1;
    public static final byte SET_SWITCH_ACTION = 2;

    protected TerminalScreen(BlockPos pos) {
        super(pos);
    }

    @Override
    protected IPosBaseTileEntity getTileEntity() {
        assert this.minecraft != null;
        assert this.minecraft.level != null;
        TileEntity tileEntity = this.minecraft.level.getBlockEntity(pos);
        if (tileEntity instanceof TerminalTile){
            return (TerminalTile) tileEntity;
        }
        assert tileEntity != null;
        throw new IllegalStateException("BlockPos of the terminal screen ("+pos+") is not associated with a correct tile entity (found tile entity of type "+tileEntity.getClass()+" ) !");
    }

    @Override
    public boolean onSwitchClicked(RailData data, double mouseX, double mouseY, Vector2f relative,int button) {
        TerminalTile tile = (TerminalTile) getTileEntity();
        // todo : send a packet to the server for each action
        TerminalScreenPacket packet = null;
        byte flag = 0;
        if (button == 0){
            //when we click on a switch with the left button of the mouse, we add or modify the switch
            if (isDisabled(data.pos)){
                tile.addSwitch(data.pos);
                packet = new TerminalScreenPacket(flag,pos,data.pos, ADD_SWITCH_ACTION);
            }else {
                BlockState state = tile.getSwitchValue(data.pos);
                if (!(state.getBlock() instanceof Switch)){
                    throw new IllegalStateException("Block position clicked is not pointing toward a switch !");
                }
                Switch switchBlock = (Switch) state.getBlock();
                EnumProperty<?> property = switchBlock.getSwitchPositionProperty();
                if (data.type == RailType.DOUBLE_SLIP){
                    boolean isLeftDownNearest = !isLeftDownNearestOnScreen(data.pos,relative,mouseX,mouseY);
                    flag = (byte) ((isLeftDownNearest)? 0b11 : 0b01);
                    Dss_Position position = (Dss_Position) state.getValue(property);
                    tile.setPosition(data.pos,state.setValue((EnumProperty<Dss_Position>)property,position.moveDssSwitch(isLeftDownNearest)));
                }else {
                    flag = 0;
                    tile.setPosition(data.pos,state.cycle(property));
                }
                packet = new TerminalScreenPacket(flag,pos,data.pos,SET_SWITCH_ACTION);
            }
        }else if (button == 1){
            //when we click on a switch with the right button of the mouse, we remove the switch if it is present
            if (!isDisabled(data.pos)){
                tile.removeSwitch(data.pos);
                packet = new TerminalScreenPacket(flag,pos,data.pos,REMOVE_SWITCH_ACTION);
            }
        }
        if (packet != null)Networking.INSTANCE.sendToServer(packet);
        return true;
    }

    @Override
    public boolean isDisabled(BlockPos pos) {
        TerminalTile tile = (TerminalTile) getTileEntity();
        return !tile.hasSwitch(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        BlockState state = super.getBlockState(pos);
        TerminalTile tile = (TerminalTile) getTileEntity();
        if (tile.hasSwitch(pos)){
            return tile.getSwitchValue(pos);
        }else {
            return state;
        }

    }

    public static void open(BlockPos pos){
        Minecraft.getInstance().setScreen(new TerminalScreen(pos));
    }
}
