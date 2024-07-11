package fr.mattmouss.switchrail.network;


import fr.mattmouss.switchrail.gui.ControllerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenControllerScreenPacket {
    private final BlockPos pos;

    public OpenControllerScreenPacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
    }

    public OpenControllerScreenPacket(BlockPos pos){
        this.pos = pos;
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()-> Minecraft.getInstance().setScreen(new ControllerScreen(pos)));
        context.get().setPacketHandled(true);
    }
}
