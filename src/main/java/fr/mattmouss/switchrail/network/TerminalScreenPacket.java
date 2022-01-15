package fr.mattmouss.switchrail.network;


import fr.mattmouss.switchrail.blocks.TerminalTile;
import fr.mattmouss.switchrail.enum_rail.Corners;
import fr.mattmouss.switchrail.gui.TerminalScreen;
import fr.mattmouss.switchrail.switchblock.Switch;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class TerminalScreenPacket {
    private final byte flag;
    private final byte action_id;
    private final BlockPos te_pos;
    private final BlockPos sw_pos;


    public TerminalScreenPacket(PacketBuffer buf) {
        flag = buf.readByte();
        te_pos = buf.readBlockPos();
        action_id = buf.readByte();
        sw_pos = buf.readBlockPos();
    }

    public TerminalScreenPacket(byte flag, BlockPos pos,BlockPos sw_pos,byte action_id){
        this.flag = flag;
        te_pos = pos;
        this.sw_pos = sw_pos;
        this.action_id = action_id;
    }

    public void toBytes(PacketBuffer buf){
        buf.writeByte(flag);
        buf.writeBlockPos(te_pos);
        buf.writeByte(action_id);
        buf.writeBlockPos(sw_pos);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            TerminalTile tile = (TerminalTile) Objects.requireNonNull(context.get().getSender()).getLevel().getBlockEntity(te_pos);
            assert tile != null;
            if (action_id == TerminalScreen.ADD_SWITCH_ACTION){ //add a switch
                tile.addSwitch(sw_pos);
            }else if (action_id == TerminalScreen.SET_SWITCH_ACTION){
                BlockState state = tile.getSwitchValue(sw_pos);
                if (!(state.getBlock() instanceof Switch)){
                    throw new IllegalStateException("Block position clicked is not pointing toward a switch !");
                }
                Switch switchBlock = (Switch) state.getBlock();
                EnumProperty<Corners> property = switchBlock.getSwitchPositionProperty();
                if (flag != 0){
                    boolean isLeftDownNearest = (flag == 3);
                    Corners corners = state.getValue(property);
                    tile.setPosition(sw_pos,state.setValue(property,corners.moveDssSwitch(isLeftDownNearest)));
                }else {
                    tile.setPosition(sw_pos,state.cycle(property));
                }
            }else if (action_id == TerminalScreen.REMOVE_SWITCH_ACTION){
                tile.removeSwitch(sw_pos);
            }
        });
        context.get().setPacketHandled(true);
    }
}
