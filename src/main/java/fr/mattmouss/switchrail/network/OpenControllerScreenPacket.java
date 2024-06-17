package fr.mattmouss.switchrail.network;


import fr.mattmouss.switchrail.gui.ControllerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenControllerScreenPacket {
    private final BlockPos pos;

    public OpenControllerScreenPacket(PacketBuffer buf) {
        pos = buf.readBlockPos();
    }

    public OpenControllerScreenPacket(BlockPos pos){
        this.pos = pos;
    }

    public void toBytes(PacketBuffer buf){
        buf.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()-> Minecraft.getInstance().setScreen(new ControllerScreen(pos)));
        context.get().setPacketHandled(true);
    }
}
