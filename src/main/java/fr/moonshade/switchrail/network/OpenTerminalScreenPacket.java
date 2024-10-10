package fr.moonshade.switchrail.network;


import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import fr.moonshade.switchrail.gui.TerminalScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenTerminalScreenPacket {
    private final BlockPos pos;
    private final int index; // -1 if no index is given. Bad usage to my mind but optional is forbidden

    public OpenTerminalScreenPacket(PacketBuffer buf) {
        pos = buf.readBlockPos();
        index = buf.readInt();
    }

    public OpenTerminalScreenPacket(BlockPos pos, int index){
        this.pos = pos;
        this.index = index;
    }

    public OpenTerminalScreenPacket(PanelCellPos cellPos){
        this.pos = cellPos.getPanelTile().getBlockPos();
        this.index = cellPos.getIndex();
    }

    public void toBytes(PacketBuffer buf){
        buf.writeBlockPos(pos);
        buf.writeInt(index);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()-> Minecraft.getInstance().setScreen(new TerminalScreen(pos,index)));
        context.get().setPacketHandled(true);
    }
}
