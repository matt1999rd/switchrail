package fr.mattmouss.switchrail.network;

import fr.mattmouss.switchrail.blocks.IPosZoomTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ChangeZoomPacket {

    private final int zoom;
    private final BlockPos tePos;
    private final boolean isX;
    public ChangeZoomPacket(int zoom, BlockPos pos,boolean isX) {
        this.zoom = zoom;
        this.tePos = pos;
        this.isX = isX;
    }

    public ChangeZoomPacket(PacketBuffer buf) {
        this.zoom = buf.readInt();
        this.tePos = buf.readBlockPos();
        this.isX = buf.readBoolean();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(zoom);
        buf.writeBlockPos(tePos);
        buf.writeBoolean(isX);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IPosZoomTileEntity te = (IPosZoomTileEntity) Objects.requireNonNull(ctx.get().getSender()).getLevel().getBlockEntity(tePos);
            assert te != null;
            if (isX)te.setZoomX(zoom);
            else te.setZoomY(zoom);
        });
        ctx.get().setPacketHandled(true);
    }
}