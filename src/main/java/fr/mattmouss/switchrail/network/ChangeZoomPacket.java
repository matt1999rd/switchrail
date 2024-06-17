package fr.mattmouss.switchrail.network;

import fr.mattmouss.switchrail.blocks.IPosZoomStorageHandler;
import fr.mattmouss.switchrail.blocks.ISRCell;
import fr.mattmouss.switchrail.other.Util;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import java.util.function.Supplier;

public class ChangeZoomPacket {

    private final int zoom;
    private final BlockPos tePos;
    private final boolean isX;
    private final int index; // -1 if no index is given. Bad usage to my mind but optional is forbidden

    public ChangeZoomPacket(int zoom, IPosZoomStorageHandler handler, boolean isX) {
        this.zoom = zoom;
        this.isX = isX;
        if (handler instanceof TileEntity){
            tePos = ((TileEntity) handler).getBlockPos();
            index = -1;
        }else if (handler instanceof ISRCell){
            ISRCell isrCell = (ISRCell)handler;
            tePos = isrCell.getPanelPos();
            index = isrCell.getCellIndex();
        }else {
            throw new IllegalStateException("Expect only tile entity or cell object in this packet");
        }
    }

    public ChangeZoomPacket(PacketBuffer buf) {
        this.zoom = buf.readInt();
        this.tePos = buf.readBlockPos();
        this.isX = buf.readBoolean();
        this.index = buf.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(zoom);
        buf.writeBlockPos(tePos);
        buf.writeBoolean(isX);
        buf.writeInt(index);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IPosZoomStorageHandler handler = Util.extractHandler(ctx,tePos,index);
            if (isX)handler.setZoomX(zoom);
            else handler.setZoomY(zoom);
        });
        ctx.get().setPacketHandled(true);
    }
}