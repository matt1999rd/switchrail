package fr.mattmouss.switchrail.blocks;

import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.mojang.datafixers.util.Pair;
import fr.mattmouss.switchrail.axle_point.CounterPoint;
import fr.mattmouss.switchrail.axle_point.WorldCounterPoints;
import fr.mattmouss.switchrail.network.Networking;
import fr.mattmouss.switchrail.network.SetAxleNumberPacket;
import fr.mattmouss.switchrail.other.Util;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

import java.util.List;

public interface IAxleCounterDetector {

    // test of the registering of cart in the world data
    default boolean isMinecartComing(WorldCounterPoints worldCP, BlockPos pos, AbstractMinecart cart){
        boolean minecartNotRegistered = worldCP.getCart(pos).isEmpty();
        if (minecartNotRegistered)worldCP.registerCart(pos,cart.getUUID());  // add cart uuid
        return minecartNotRegistered;
    }

    default boolean isMinecartLeaving(WorldCounterPoints worldCP,BlockPos railPos, AbstractMinecart cart){
        boolean res = cart.getX() < railPos.getX() || cart.getX() > railPos.getX() + 1 ||
                cart.getZ() < railPos.getZ() || cart.getZ() > railPos.getZ() + 1 ;
        if (res)worldCP.removeCart(railPos);
        return res;
    }

    // correction need to be made on direction of the minecart because on turn, cart may be in the wrong direction
    default Direction getMotionDirection(AbstractMinecart cart, boolean isLeaving, RailShape shape){
        Vec3 motion = cart.getDeltaMovement();
        Direction motionRawDirection = Direction.getNearest(motion.x,0,motion.z);
        Pair<Direction,Direction> pair = Util.getDirections(shape);
        if (pair.getFirst() == pair.getSecond().getOpposite()){
            return motionRawDirection; //no problem with direction here : it is a straight line rail shape
        }

        //when leaving a rail the movement direction must be one of the pair of direction of the shape
        if (isLeaving){
            if (motionRawDirection == pair.getFirst().getOpposite()){
                return pair.getSecond();
            } else if (motionRawDirection == pair.getSecond().getOpposite()){
                return pair.getFirst();
            } else {
                return motionRawDirection;
            }
        // when coming on rail the movement is reversed and origin are opposite direction of the motion
        // therefore motion in the two direction of the shape make no sense
        }else {
            if (motionRawDirection == pair.getFirst()){
                return pair.getSecond().getOpposite();
            }else if (motionRawDirection == pair.getSecond()){
                return pair.getFirst().getOpposite();
            } else {
                return motionRawDirection;
            }
        }
    }

    default void onMinecartLimitPass(Level world, WorldCounterPoints worldCP, BlockPos pos,Direction side,boolean isLeaving){
        List<CounterPoint> sideCPoints = worldCP.getCounterPoints(pos,side,isLeaving);
        if (sideCPoints == null)return;
        if (!sideCPoints.isEmpty()){
            sideCPoints.forEach(cp->{
                BlockPos acPos = cp.getACPos();
                int index = cp.getIndex();
                boolean directionMatch = cp.countIfMinecartArrive() == !isLeaving;
                boolean addAxle = (!cp.isAddingAxle() && cp.isBidirectional() && !directionMatch) ||
                        cp.isAddingAxle() && (!cp.isBidirectional() || directionMatch);
                ICounterHandler handler;
                if (index == -1){
                    handler = Util.getAxleTileEntity(world,acPos);
                }else{
                    BlockEntity te = world.getBlockEntity(acPos);
                    if (te instanceof PanelTile panelTile){
                        handler = (ICounterHandler) panelTile.getIPanelCell(PanelCellPos.fromIndex(panelTile,index));
                    }else {
                        throw new IllegalStateException("Index is not -1 and we get a tile entity which is not a panel tile !");
                    }
                }
                assert handler != null;
                if (addAxle){
                    handler.addAxle();
                    // we add or remove axle server side => need to add or remove axle on all client side as well for axle counter tile
                    // with axle counter cell, update server side are brought down by the function IPanelCell#writeNBT
                    if (handler instanceof AxleCounterTile) Networking.INSTANCE.send(PacketDistributor.ALL.noArg(), new SetAxleNumberPacket(acPos, 1,index));
                }else {
                    handler.removeAxle();
                    if (handler instanceof AxleCounterTile) Networking.INSTANCE.send(PacketDistributor.ALL.noArg(), new SetAxleNumberPacket(acPos, -1,index));
                }
            });
        }
    }

    default void onMinecartPass(Level world, BlockPos pos, AbstractMinecart cart, RailShape shape){
        WorldCounterPoints worldCP = Util.getWorldCounterPoint(world);
        boolean isMinecartComing = isMinecartComing(worldCP,pos,cart);
        boolean isMinecartLeaving = isMinecartLeaving(worldCP,pos,cart);
        if (isMinecartComing){
            System.out.println("Minecart "+cart.getUUID()+" is coming on block !");
            Direction motionDirection = getMotionDirection(cart,false,shape);
            onMinecartLimitPass(world,worldCP,pos,motionDirection.getOpposite(),false);
        }
        if (isMinecartLeaving) {
            System.out.println("Minecart "+cart.getUUID()+ " is leaving block !");
            Direction motionDirection = getMotionDirection(cart,true,shape);
            onMinecartLimitPass(world,worldCP,pos,motionDirection,true);
        }
    }

    default void onRemoveAxleCounter(Level world, BlockState oldState, BlockState actualState, BlockPos cpPos){
        // this function is done only server side
        if (actualState.getBlock() != oldState.getBlock()) {
            WorldCounterPoints worldCP = Util.getWorldCounterPoint(world);
            worldCP.onCPBlockRemove(cpPos);
        }
    }

}
