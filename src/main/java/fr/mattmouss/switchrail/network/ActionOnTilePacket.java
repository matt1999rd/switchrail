package fr.mattmouss.switchrail.network;

import fr.mattmouss.switchrail.blocks.TerminalTile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ActionOnTilePacket {
    private final boolean isPowered;
    private final BlockPos terminalPos;
    public ActionOnTilePacket(PacketBuffer buffer){
        isPowered = buffer.readBoolean();
        terminalPos = buffer.readBlockPos();
    }

    public ActionOnTilePacket(BlockPos pos, boolean isPowered){
        this.isPowered = isPowered;
        terminalPos = pos;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeBoolean(isPowered);
        buffer.writeBlockPos(terminalPos);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(
                ()->{
                    if (Minecraft.getInstance().level != null) {
                        TileEntity tileEntity = Minecraft.getInstance().level.getBlockEntity(terminalPos);
                        if (!(tileEntity instanceof TerminalTile))
                            throw new IllegalStateException("The tile entity of the blockpos is not a Terminal Tile !");
                        TerminalTile tile = (TerminalTile) tileEntity;
                        if (isPowered) {
                            tile.actionOnPowered();
                        } else {
                            tile.actionOnUnpowered();
                        }
                    }
                });
        contextSupplier.get().setPacketHandled(true);
    }
}
