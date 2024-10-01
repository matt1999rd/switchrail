package fr.moonshade.switchrail.network;


import fr.moonshade.switchrail.blocks.IPosZoomStorageHandler;
import fr.moonshade.switchrail.blocks.ISRCell;
import fr.moonshade.switchrail.other.Util;
import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;


import java.util.function.Supplier;

public class ChangePosPacket {
    private final int newPos;
    private final Direction.Axis axis;
    private final BlockPos tePos;
    private final int index; // -1 if no index is given. Bad usage to my mind but optional is forbidden


    public ChangePosPacket(FriendlyByteBuf buf) {
        newPos = buf.readInt();
        axis = buf.readEnum(Direction.Axis.class);
        tePos = buf.readBlockPos();
        index = buf.readInt();
    }

    public ChangePosPacket(int newPos,IPosZoomStorageHandler handler,Direction.Axis axis){
        this.newPos = newPos;
        this.axis = axis;
        if (handler instanceof BlockEntity){
            tePos = ((BlockEntity) handler).getBlockPos();
            index = -1;
        }else if (handler instanceof ISRCell){
            tePos = ((ISRCell) handler).getPanelPos();
            index = ((ISRCell) handler).getCellIndex();
        }else {
            throw new IllegalStateException("Expect only handler (tile entity or cell object) in this packet");
        }
    }

    public void toBytes(FriendlyByteBuf buf){
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
