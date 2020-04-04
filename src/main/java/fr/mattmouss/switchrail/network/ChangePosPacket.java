package fr.mattmouss.switchrail.network;


import fr.mattmouss.switchrail.blocks.ControllerTile;
import net.minecraft.network.PacketBuffer;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;


import java.util.function.Supplier;

public class ChangePosPacket {
    private final int dir_id;
    private final BlockPos te_pos;


    public ChangePosPacket(PacketBuffer buf) {
        dir_id = buf.readInt();
        te_pos = buf.readBlockPos();
    }

    public ChangePosPacket(Direction direction,BlockPos pos){
        dir_id = direction.getIndex();
        te_pos = pos;
    }

    public void toBytes(PacketBuffer buf){
        buf.writeInt(dir_id);
        buf.writeBlockPos(te_pos);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            ControllerTile te = (ControllerTile) context.get().getSender().getServerWorld().getTileEntity(te_pos);
            BlockPos pos_base =te.getPosBase();
            Direction dir = Direction.byIndex(dir_id);
            BlockPos new_pos_base = pos_base.offset(dir);
            System.out.println("set of the new Position : "+new_pos_base);
            te.setX(new_pos_base.getX());
            te.setY(new_pos_base.getY());
            te.setZ(new_pos_base.getZ());
        });
        context.get().setPacketHandled(true);
    }
}
