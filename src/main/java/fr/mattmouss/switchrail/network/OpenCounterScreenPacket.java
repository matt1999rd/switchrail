package fr.mattmouss.switchrail.network;

import fr.mattmouss.switchrail.axle_point.CounterPointInfo;
import fr.mattmouss.switchrail.gui.CounterScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenCounterScreenPacket {
    private final BlockPos acPos;
    private final CounterPointInfo cpInfo;
    public OpenCounterScreenPacket(BlockPos acPos, CounterPointInfo cpInfo) {
        this.acPos = acPos;
        this.cpInfo = cpInfo;
    }

    public OpenCounterScreenPacket(PacketBuffer buf) {
        acPos = buf.readBlockPos();
        cpInfo = new CounterPointInfo(buf);
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(acPos);
        cpInfo.toBytes(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> Minecraft.getInstance().setScreen(new CounterScreen(acPos, cpInfo)));
        ctx.get().setPacketHandled(true);
    }
}