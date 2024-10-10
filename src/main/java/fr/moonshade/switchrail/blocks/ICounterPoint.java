package fr.moonshade.switchrail.blocks;

import fr.moonshade.switchrail.axle_point.CounterPointInfo;
import fr.moonshade.switchrail.axle_point.WorldCounterPoints;
import fr.moonshade.switchrail.network.Networking;
import fr.moonshade.switchrail.network.OpenCounterScreenPacket;
import fr.moonshade.switchrail.other.Util;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;

public interface ICounterPoint {
    default void onACRemove(World world, BlockPos pos){
        // this function is done only server side
        WorldCounterPoints worldCP = Util.getWorldCounterPoint(world);
        worldCP.onACRemove(pos);
    }

    default void onBlockClicked(World world, BlockPos pos, ServerPlayerEntity player, int index){
        CounterPointInfo cpInfo = getCPInfo(world,pos.immutable(),index);
        //send a packet to client to open screen
        Networking.INSTANCE.sendTo(new OpenCounterScreenPacket(pos, cpInfo,index),player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    default CounterPointInfo getCPInfo(World world, BlockPos acPos,int index){
        WorldCounterPoints worldCP = Util.getWorldCounterPoint(world);
        return worldCP.getCPInfo(acPos,index);
    }
}
