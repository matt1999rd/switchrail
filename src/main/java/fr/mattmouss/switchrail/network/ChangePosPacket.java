package fr.mattmouss.switchrail.network;


import fr.mattmouss.switchrail.blocks.ControllerTile;
import fr.mattmouss.switchrail.blocks.IPosBaseTileEntity;
import net.minecraft.network.PacketBuffer;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;


import java.util.Objects;
import java.util.function.Supplier;

public class ChangePosPacket {
    private final int dir_id;
    private final BlockPos te_pos;


    public ChangePosPacket(PacketBuffer buf) {
        dir_id = buf.readInt();
        te_pos = buf.readBlockPos();
    }

    public ChangePosPacket(Direction direction,BlockPos pos){
        dir_id = direction.get3DDataValue();
        te_pos = pos;
    }

    public void toBytes(PacketBuffer buf){
        buf.writeInt(dir_id);
        buf.writeBlockPos(te_pos);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            IPosBaseTileEntity te = (IPosBaseTileEntity) Objects.requireNonNull(context.get().getSender()).getLevel().getBlockEntity(te_pos);
            assert te != null;
            Direction dir = Direction.from3DDataValue(dir_id);
            te.changePosBase(dir);
        });
        context.get().setPacketHandled(true);
    }
}
