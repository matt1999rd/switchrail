package fr.moonshade.switchrail.network;

import fr.moonshade.switchrail.blocks.TerminalTile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ActionOnTilePacket {
    private final boolean isPowered;
    private final BlockPos terminalPos;
    public ActionOnTilePacket(FriendlyByteBuf buffer){
        isPowered = buffer.readBoolean();
        terminalPos = buffer.readBlockPos();
    }

    public ActionOnTilePacket(BlockPos pos, boolean isPowered){
        this.isPowered = isPowered;
        terminalPos = pos;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBoolean(isPowered);
        buffer.writeBlockPos(terminalPos);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(
                ()->{
                    if (Minecraft.getInstance().level != null) {
                        BlockEntity tileEntity = Minecraft.getInstance().level.getBlockEntity(terminalPos);
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
