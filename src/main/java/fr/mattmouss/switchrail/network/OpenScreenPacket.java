package fr.mattmouss.switchrail.network;


import fr.mattmouss.switchrail.enum_rail.ScreenType;
import fr.mattmouss.switchrail.gui.ControllerScreen;
import fr.mattmouss.switchrail.gui.TerminalScreen;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenScreenPacket {
    //todo : return to the last implementation in older version (without counter screen)
    private final BlockPos te_pos;
    private final ScreenType screenType;

    public OpenScreenPacket(PacketBuffer buf) {
        te_pos = buf.readBlockPos();
        screenType = ScreenType.readBuf(buf);
    }

    public OpenScreenPacket(BlockPos pos, ScreenType type){
        te_pos = pos;
        this.screenType = type;
    }

    public void toBytes(PacketBuffer buf){
        buf.writeBlockPos(te_pos);
        screenType.write(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()-> screenType.open(te_pos));
        context.get().setPacketHandled(true);
    }
}
