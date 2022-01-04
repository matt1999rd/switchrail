package fr.mattmouss.switchrail.network;


import fr.mattmouss.switchrail.gui.ControllerScreen;
import fr.mattmouss.switchrail.gui.TerminalScreen;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenScreenPacket {
    private final BlockPos te_pos;
    private final boolean isFromController;

    public OpenScreenPacket(PacketBuffer buf) {
        te_pos = buf.readBlockPos();
        isFromController = buf.readBoolean();
    }

    public OpenScreenPacket(BlockPos pos,boolean isFromController){
        te_pos = pos;
        this.isFromController = isFromController;
    }

    public void toBytes(PacketBuffer buf){
        buf.writeBlockPos(te_pos);
        buf.writeBoolean(isFromController);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            if (isFromController)ControllerScreen.open(te_pos);
            else TerminalScreen.open(te_pos);
        });
        context.get().setPacketHandled(true);
    }
}
