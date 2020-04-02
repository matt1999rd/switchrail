package fr.mattmouss.switchrail.network;


import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import fr.mattmouss.gates.tileentity.TurnStileTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ChangeIdPacket {
    private final BlockPos pos;
    private final int key_id;


    public ChangeIdPacket(PacketBuffer buf) {
        pos = buf.readBlockPos();
        key_id = buf.readInt();
    }

    public ChangeIdPacket(BlockPos pos_in, int id){
        pos = pos_in;
        key_id = id;
    }

    public void toBytes(PacketBuffer buf){
        buf.writeBlockPos(pos);
        buf.writeInt(key_id);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        AtomicInteger val = new AtomicInteger(0);
        context.get().enqueueWork(()->{
            if (key_id == -1) {
                TileEntity te = context.get().getSender().getServerWorld().getTileEntity(pos);
                System.out.println("packet handled : no key found");
                if (te instanceof TollGateTileEntity) {
                    ((TollGateTileEntity) te).changeId();
                    val.set(((TollGateTileEntity) te).getId());
                } else if (te instanceof TurnStileTileEntity) {
                    ((TurnStileTileEntity) te).changeId();
                    val.set(((TurnStileTileEntity) te).getId());
                } else {
                    return;
                }
            }else {
                TileEntity te = context.get().getSender().getServerWorld().getTileEntity(pos);
                System.out.println("packet handled : a key found with id :"+key_id);
                if (te instanceof TollGateTileEntity) {
                    ((TollGateTileEntity) te).setId(key_id);
                    val.set(((TollGateTileEntity) te).getId());
                } else if (te instanceof TurnStileTileEntity) {
                    ((TurnStileTileEntity) te).setId(key_id);
                    val.set(((TurnStileTileEntity) te).getId());
                } else {
                    return;
                }
            }
        });
        Networking.INSTANCE.send(PacketDistributor.PLAYER.with(()-> {
            return context.get().getSender();
        }),new SetIdPacket(pos,val.get()));
        context.get().setPacketHandled(true);
    }
}
