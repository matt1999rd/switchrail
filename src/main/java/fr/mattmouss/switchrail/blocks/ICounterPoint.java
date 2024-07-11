package fr.mattmouss.switchrail.blocks;

import fr.mattmouss.switchrail.axle_point.CounterPointInfo;
import fr.mattmouss.switchrail.axle_point.WorldCounterPoints;
import fr.mattmouss.switchrail.network.Networking;
import fr.mattmouss.switchrail.network.OpenCounterScreenPacket;
import fr.mattmouss.switchrail.other.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkDirection;

public interface ICounterPoint {
    default void onACRemove(Level world, BlockPos pos){
        // this function is done only server side
        WorldCounterPoints worldCP = Util.getWorldCounterPoint(world);
        worldCP.onACRemove(pos);
    }

    default void onBlockClicked(Level world, BlockPos pos, ServerPlayer player, int index){
        CounterPointInfo cpInfo = getCPInfo(world,pos.immutable(),index);
        //send a packet to client to open screen
        Networking.INSTANCE.sendTo(new OpenCounterScreenPacket(pos, cpInfo,index),player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    default CounterPointInfo getCPInfo(Level world, BlockPos acPos,int index){
        WorldCounterPoints worldCP = Util.getWorldCounterPoint(world);
        return worldCP.getCPInfo(acPos,index);
    }
}
