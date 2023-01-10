package fr.mattmouss.switchrail.network;

import fr.mattmouss.switchrail.axle_point.CounterPoint;
import fr.mattmouss.switchrail.axle_point.WorldCounterPoints;
import fr.mattmouss.switchrail.other.Util;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class UpdateCounterPointPacket {
    private final BlockPos cpPos;
    private final int type;
    private final Optional<CounterPoint> counterPoint;
    private final Optional<BlockPos> acPos;
    private final Optional<Direction> side;

    public UpdateCounterPointPacket(BlockPos cpPos, int type, CounterPoint cp,BlockPos acPos,Direction side) {
        this.cpPos = cpPos;
        this.type = type;
        this.counterPoint = Optional.ofNullable(cp);
        this.acPos = Optional.ofNullable(acPos);
        this.side = Optional.ofNullable(side);
    }

    public UpdateCounterPointPacket(PacketBuffer buf) {
        this.cpPos = buf.readBlockPos();
        this.type = buf.readInt();
        if (type == WorldCounterPoints.ADD_COUNTER_PT){
            this.counterPoint = Optional.of(new CounterPoint(buf));
            this.acPos = Optional.empty();
            this.side = Optional.empty();
        }else {
            this.counterPoint = Optional.empty();
            this.acPos = Optional.of(buf.readBlockPos());
            this.side = Optional.of(buf.readEnum(Direction.class));
        }
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(cpPos);
        buf.writeInt(type);
        counterPoint.ifPresent(point -> point.toBytes(buf));
        acPos.ifPresent(buf::writeBlockPos);
        side.ifPresent(buf::writeEnum);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World world = Objects.requireNonNull(ctx.get().getSender()).getLevel();
            WorldCounterPoints worldCP = Util.getWorldCounterPoint(world);
            worldCP.doActionServerSide(cpPos,type,counterPoint,acPos,side);
        });
        ctx.get().setPacketHandled(true);
    }
}