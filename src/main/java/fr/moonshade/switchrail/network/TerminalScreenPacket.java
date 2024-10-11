package fr.moonshade.switchrail.network;


import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import fr.moonshade.switchrail.blocks.ITerminalHandler;
import fr.moonshade.switchrail.enum_rail.Corners;
import fr.moonshade.switchrail.gui.TerminalScreen;
import fr.moonshade.switchrail.switchblock.Switch;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class TerminalScreenPacket {
    private final byte flag;
    private final byte action_id;
    private final BlockPos te_pos;
    private final BlockPos sw_pos;
    private final int index; // -1 if no index is given. Bad usage to my mind but optional is forbidden.


    public TerminalScreenPacket(FriendlyByteBuf buf) {
        flag = buf.readByte();
        te_pos = buf.readBlockPos();
        action_id = buf.readByte();
        sw_pos = buf.readBlockPos();
        index = buf.readInt();
    }

    public TerminalScreenPacket(byte flag, BlockPos pos, int index, BlockPos sw_pos, byte action_id){
        this.flag = flag;
        te_pos = pos;
        this.sw_pos = sw_pos;
        this.action_id = action_id;
        this.index = index;
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeByte(flag);
        buf.writeBlockPos(te_pos);
        buf.writeByte(action_id);
        buf.writeBlockPos(sw_pos);
        buf.writeInt(index);

    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            ITerminalHandler terminalHandler;
            if (index != -1){
                PanelTile tile = (PanelTile) Objects.requireNonNull(context.get().getSender()).getLevel().getBlockEntity(te_pos);
                assert tile != null;
                terminalHandler = (ITerminalHandler) tile.getIPanelCell(PanelCellPos.fromIndex(tile,index));
            } else {
                BlockEntity tile = Objects.requireNonNull(context.get().getSender()).getLevel().getBlockEntity(te_pos);
                if (tile instanceof ITerminalHandler) {
                    terminalHandler = (ITerminalHandler) tile;
                } else {
                    throw new IllegalStateException("Error in Client Server Communication : the tile entity get is not a terminal handler");
                }
            }
            assert terminalHandler != null;
            if (action_id == TerminalScreen.ADD_SWITCH_ACTION) { //add a switch
                terminalHandler.addSwitch(sw_pos);
            } else if (action_id == TerminalScreen.SET_SWITCH_ACTION) {
                BlockState state = terminalHandler.getSwitchValue(sw_pos);
                if (!(state.getBlock() instanceof Switch)) {
                    throw new IllegalStateException("Block position clicked is not pointing toward a switch !");
                }
                Switch switchBlock = (Switch) state.getBlock();
                EnumProperty<Corners> property = switchBlock.getSwitchPositionProperty();
                if (flag != 0) {
                    boolean isLeftDownNearest = (flag == 3);
                    Corners corners = state.getValue(property);
                    terminalHandler.setPosition(sw_pos, state.setValue(property, corners.moveDssSwitch(isLeftDownNearest)));
                } else {
                    terminalHandler.setPosition(sw_pos, state.cycle(property));
                }
            } else if (action_id == TerminalScreen.REMOVE_SWITCH_ACTION) {
                terminalHandler.removeSwitch(sw_pos);
            }
        });
        context.get().setPacketHandled(true);
    }
}
