package fr.mattmouss.switchrail.network;


import fr.mattmouss.switchrail.blocks.IPosZoomTileEntity;
import net.minecraft.network.PacketBuffer;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;


import java.util.Objects;
import java.util.function.Supplier;

public class ChangePosPacket {
    private final int newPos;
    private final Direction.Axis axis;
    private final BlockPos te_pos;


    public ChangePosPacket(PacketBuffer buf) {
        newPos = buf.readInt();
        axis = buf.readEnum(Direction.Axis.class);
        te_pos = buf.readBlockPos();
    }

    public ChangePosPacket(int newPos,BlockPos pos,Direction.Axis axis){
        this.newPos = newPos;
        this.axis = axis;
        te_pos = pos;
    }

    public void toBytes(PacketBuffer buf){
        buf.writeInt(newPos);
        buf.writeEnum(axis);
        buf.writeBlockPos(te_pos);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            IPosZoomTileEntity te = (IPosZoomTileEntity) Objects.requireNonNull(context.get().getSender()).getLevel().getBlockEntity(te_pos);
            assert te != null;
            te.setBasePos(axis,newPos);
        });
        context.get().setPacketHandled(true);
    }
}
