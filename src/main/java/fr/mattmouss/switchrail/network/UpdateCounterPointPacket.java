package fr.mattmouss.switchrail.network;

import fr.mattmouss.switchrail.axle_point.CounterPoint;
import fr.mattmouss.switchrail.axle_point.WorldCounterPoints;
import fr.mattmouss.switchrail.other.Util;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class UpdateCounterPointPacket {
    private final BlockPos cpPos;
    private final int type;
    @Nullable
    private final CounterPoint counterPoint;
    private final BlockPos acPos;
    private final Direction side;
    private final int index; // -1 if no index is given. Bad usage to my mind but optional is forbidden

    public UpdateCounterPointPacket(BlockPos cpPos, int type, CounterPoint cp,BlockPos acPos,Direction side,int index) {
        this.cpPos = cpPos;
        this.type = type;
        this.counterPoint = cp;
        this.acPos = acPos;
        this.side = side;
        this.index = index;
    }

    public UpdateCounterPointPacket(PacketBuffer buf) {
        this.cpPos = buf.readBlockPos();
        this.type = buf.readInt();
        this.index = buf.readInt();
        if (type == WorldCounterPoints.ADD_COUNTER_PT){
            this.counterPoint = new CounterPoint(buf);
            this.acPos = null;
            this.side = null;
        }else {
            this.counterPoint = null;
            this.acPos = buf.readBlockPos();
            this.side = buf.readEnum(Direction.class);
        }
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(cpPos);
        buf.writeInt(type);
        buf.writeInt(index);
        if (type == WorldCounterPoints.ADD_COUNTER_PT){
            assert counterPoint != null;
            counterPoint.toBytes(buf);
        } else {
            buf.writeBlockPos(acPos);
            buf.writeEnum(side);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World world = Objects.requireNonNull(ctx.get().getSender()).getLevel();
            WorldCounterPoints worldCP = Util.getWorldCounterPoint(world);
            worldCP.doActionServerSide(cpPos,type,counterPoint,acPos,side,index);
        });
        ctx.get().setPacketHandled(true);
    }
}