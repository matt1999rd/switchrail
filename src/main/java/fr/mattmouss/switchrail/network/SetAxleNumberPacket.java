package fr.mattmouss.switchrail.network;

import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import fr.mattmouss.switchrail.blocks.AxleCounterTile;
import fr.mattmouss.switchrail.blocks.ICounterHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.awt.*;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class SetAxleNumberPacket {
    private final BlockPos acPos;
    private final int axleOffset;
    private final int index; // -1 if no index is given. Bad usage to my mind but optional is forbidden

    public SetAxleNumberPacket(BlockPos acPos,int axleOffset,int index) {
        this.acPos = acPos;
        this.axleOffset = axleOffset;
        this.index = index;
    }

    public SetAxleNumberPacket(PacketBuffer buf) {
        this.acPos = buf.readBlockPos();
        this.axleOffset = buf.readInt();
        this.index = buf.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(acPos);
        buf.writeInt(axleOffset);
        buf.writeInt(index);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World world;
            if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER){
                world = Objects.requireNonNull(ctx.get().getSender()).getLevel();
                handlePacket(world,acPos,axleOffset,index);
            }else if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT){
                world = Minecraft.getInstance().level;
                assert world != null;
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->handlePacket(world,acPos,axleOffset,index));
            }else {
                throw new IllegalStateException("Packet received with incorrect network direction : "
                        +ctx.get().getDirection()+" . Expect only PLAY_TO_SERVER or PLAY_TO_CLIENT !");
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static void handlePacket(World world,BlockPos acPos,int axleOffset,int index){
        TileEntity tile = world.getBlockEntity(acPos);
        ICounterHandler handler;
        if (tile instanceof AxleCounterTile){
            if (index != -1) {
                Logger.getGlobal().warning("Index is not -1 and axle tile entity is configured : there might be a problem !");
            }
            handler = (ICounterHandler) tile;
        }else if (tile instanceof PanelTile) {
            if (index == -1){
                throw new IllegalStateException("Error in packet transmission : the index of the axle counter cell is not given !");
            }
            PanelTile panelTile = (PanelTile) tile;
            handler = (ICounterHandler) panelTile.getIPanelCell(PanelCellPos.fromIndex(panelTile,index));
        }else {
            throw new IllegalStateException("Error in packet transmission : the tile entity obtained is not an axle counter tile nor panel tile. Get this instead : "+tile);
        }
        assert handler != null;
        if (axleOffset == 0){ // when offset is 0 : it is not information no action to be done but the action remove all axle
            handler.freePoint();
        }else if (axleOffset == 1){
            handler.addAxle();
        }else if (axleOffset == -1){
            handler.removeAxle();
        }else {
            Logger.getGlobal().warning("Unexpected offset given in Set Axle Packet : "+axleOffset);
        }
    }
}