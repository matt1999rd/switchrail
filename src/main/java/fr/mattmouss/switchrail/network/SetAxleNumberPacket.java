package fr.mattmouss.switchrail.network;

import fr.mattmouss.switchrail.blocks.AxleCounterTile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class SetAxleNumberPacket {
    private final BlockPos acPos;
    private final int axleOffset;

    public SetAxleNumberPacket(BlockPos acPos,int axleOffset) {
        this.acPos = acPos;
        this.axleOffset = axleOffset;
    }

    public SetAxleNumberPacket(PacketBuffer buf) {
        this.acPos = buf.readBlockPos();
        this.axleOffset = buf.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(acPos);
        buf.writeInt(axleOffset);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World world;
            if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER){
                world = Objects.requireNonNull(ctx.get().getSender()).getLevel();
                handlePacket(world,acPos,axleOffset);
            }else if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT){
                world = Minecraft.getInstance().level;
                assert world != null;
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->handlePacket(world,acPos,axleOffset));
            }else {
                throw new IllegalStateException("Packet received with incorrect network direction : "
                        +ctx.get().getDirection()+" . Expect only PLAY_TO_SERVER or PLAY_TO_CLIENT !");
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static void handlePacket(World world,BlockPos acPos,int axleOffset){
        TileEntity tile = world.getBlockEntity(acPos);
        if (tile instanceof AxleCounterTile){
            AxleCounterTile acTile = (AxleCounterTile) tile;
            if (axleOffset == 0){ // when offset is 0 : it is not information no action to be done but the action remove all axle
                acTile.freePoint();
            }else if (axleOffset == 1){
                acTile.addAxle();
            }else if (axleOffset == -1){
                acTile.removeAxle();
            }else {
                Logger.getGlobal().warning("Unexpected offset given in Set Axle Packet : "+axleOffset);
            }
        }else {
            throw new IllegalStateException("Error in packet transmission : the tile entity obtained is not an axle counter tile. Get this instead : "+tile);
        }
    }
}