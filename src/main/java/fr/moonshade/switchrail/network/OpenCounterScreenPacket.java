package fr.moonshade.switchrail.network;

import fr.moonshade.switchrail.axle_point.CounterPointInfo;
import fr.moonshade.switchrail.gui.CounterScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenCounterScreenPacket {
    private final BlockPos acPos;
    private final CounterPointInfo cpInfo;
    private final int index; // -1 if no index is given. Bad usage to my mind but optional is forbidden
    public OpenCounterScreenPacket(BlockPos acPos, CounterPointInfo cpInfo,int index) {
        this.acPos = acPos;
        this.cpInfo = cpInfo;
        this.index = index;
    }

    public OpenCounterScreenPacket(PacketBuffer buf) {
        acPos = buf.readBlockPos();
        index = buf.readInt();
        cpInfo = new CounterPointInfo(buf);
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(acPos);
        buf.writeInt(index);
        cpInfo.toBytes(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> Minecraft.getInstance().setScreen(new CounterScreen(acPos, cpInfo, index)));
        ctx.get().setPacketHandled(true);
    }
}