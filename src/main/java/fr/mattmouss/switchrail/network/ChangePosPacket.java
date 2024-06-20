package fr.mattmouss.switchrail.network;


import fr.mattmouss.switchrail.blocks.IPosZoomStorageHandler;
import fr.mattmouss.switchrail.blocks.ISRCell;
import fr.mattmouss.switchrail.other.Util;
import net.minecraft.network.PacketBuffer;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;



import java.util.function.Supplier;

public class ChangePosPacket {
    private final int newPos;
    private final Direction.Axis axis;
    private final BlockPos tePos;
    private final int index; // -1 if no index is given. Bad usage to my mind but optional is forbidden


    public ChangePosPacket(PacketBuffer buf) {
        newPos = buf.readInt();
        axis = buf.readEnum(Direction.Axis.class);
        tePos = buf.readBlockPos();
        index = buf.readInt();
    }

    public ChangePosPacket(int newPos,IPosZoomStorageHandler handler,Direction.Axis axis){
        this.newPos = newPos;
        this.axis = axis;
        if (handler instanceof TileEntity){
            tePos = ((TileEntity) handler).getBlockPos();
            index = -1;
        }else if (handler instanceof ISRCell){
            tePos = ((ISRCell) handler).getPanelPos();
            index = ((ISRCell) handler).getCellIndex();
        }else {
            throw new IllegalStateException("Expect only handler (tile entity or cell object) in this packet");
        }
    }

    public void toBytes(PacketBuffer buf){
        buf.writeInt(newPos);
        buf.writeEnum(axis);
        buf.writeBlockPos(tePos);
        buf.writeInt(index);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            IPosZoomStorageHandler handler = Util.extractHandler(context,tePos,index);
            handler.setBasePos(axis,newPos);
        });
        context.get().setPacketHandled(true);
    }
}
