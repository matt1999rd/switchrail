package fr.moonshade.switchrail.network;

import fr.moonshade.switchrail.blocks.IPosZoomStorageHandler;
import fr.moonshade.switchrail.blocks.ISRCell;
import fr.moonshade.switchrail.other.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class ChangeZoomPacket {

    private final int zoom;
    private final BlockPos tePos;
    private final boolean isX;
    private final int index; // -1 if no index is given. Bad usage to my mind but optional is forbidden

    public ChangeZoomPacket(int zoom, IPosZoomStorageHandler handler, boolean isX) {
        this.zoom = zoom;
        this.isX = isX;
        if (handler instanceof BlockEntity){
            tePos = ((BlockEntity) handler).getBlockPos();
            index = -1;
        }else if (handler instanceof ISRCell){
            ISRCell isrCell = (ISRCell)handler;
            tePos = isrCell.getPanelPos();
            index = isrCell.getCellIndex();
        }else {
            throw new IllegalStateException("Expect only tile entity or cell object in this packet");
        }
    }

    public ChangeZoomPacket(FriendlyByteBuf buf) {
        this.zoom = buf.readInt();
        this.tePos = buf.readBlockPos();
        this.isX = buf.readBoolean();
        this.index = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
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