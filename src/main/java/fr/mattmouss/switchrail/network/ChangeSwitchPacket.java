package fr.mattmouss.switchrail.network;

import fr.mattmouss.switchrail.enum_rail.Corners;
import fr.mattmouss.switchrail.switchblock.Switch;
import fr.mattmouss.switchrail.switchblock.SwitchDoubleSlip;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ChangeSwitchPacket {
    private final BlockPos sw_pos;
    private final byte flag;
    //the byte flag is stocking two booleans : ab :
    // a = ld_nearest ? 1 : 0;
    // b = (data.type.getMeta() == 1) ? 1 :0;
    public ChangeSwitchPacket(BlockPos pos, byte flag_in){
        sw_pos = pos;
        flag = flag_in;
    }
    public ChangeSwitchPacket(PacketBuffer buf){
        sw_pos = buf.readBlockPos();
        flag = buf.readByte();
    }

    public void toBytes(PacketBuffer buf){
        buf.writeBlockPos(sw_pos);
        buf.writeByte(flag);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            ServerWorld world = Objects.requireNonNull(context.get().getSender()).getLevel();
            BlockState state = world.getBlockState(sw_pos);
            Block block = state.getBlock();
            if (block instanceof Switch) {
                if (flag == 0) {
                    Switch sw = (Switch) block;
                    System.out.println("change of switch done in packet !!");
                    sw.updatePoweredState(world, state, sw_pos, null, 7, true);
                }else {
                    //we have a double slip position
                    Corners tjd_position = state.getValue(SwitchDoubleSlip.DSS_POSITION);
                    SwitchDoubleSlip switch_tjd = (SwitchDoubleSlip)(state.getBlock());
                    boolean ld_nearest = (flag == 3);
                    // the flag can only be 3 or 1 because first bit is always 1 3 = 0b11 which means ld_nearest = true
                    switch_tjd.updatePowerState(world,state,sw_pos,7,ld_nearest,tjd_position);
                }
            }

        });
        context.get().setPacketHandled(true);
    }
}
