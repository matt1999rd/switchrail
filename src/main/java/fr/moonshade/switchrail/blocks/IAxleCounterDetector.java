package fr.moonshade.switchrail.blocks;

import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.mojang.datafixers.util.Pair;
import fr.moonshade.switchrail.axle_point.CounterPoint;
import fr.moonshade.switchrail.axle_point.WorldCounterPoints;
import fr.moonshade.switchrail.network.Networking;
import fr.moonshade.switchrail.network.SetAxleNumberPacket;
import fr.moonshade.switchrail.other.Util;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.List;

public interface IAxleCounterDetector {

    // test of the registering of cart in the world data
    default boolean isMinecartComing(WorldCounterPoints worldCP, BlockPos pos, AbstractMinecartEntity cart){
        boolean res = !worldCP.getCart(pos).isPresent();
        if (res)worldCP.onCartPassing(pos,cart.getUUID());  // add cart uuid
        return res;
    }

    default boolean isMinecartLeaving(WorldCounterPoints worldCP,BlockPos railPos, AbstractMinecartEntity cart){
        boolean res = cart.getX() < railPos.getX() || cart.getX() > railPos.getX() + 1 ||
                cart.getZ() < railPos.getZ() || cart.getZ() > railPos.getZ() + 1 ;
        if (res)worldCP.onCartLeaving(railPos);
        return res;
    }

    // correction need to be made on direction of the minecart because on turn, cart may be in the wrong direction
    default Direction getMotionDirection(AbstractMinecartEntity cart, boolean isLeaving, RailShape shape){
        Vector3d motion = cart.getDeltaMovement();
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

    default void onMinecartLimitPass(World world, WorldCounterPoints worldCP, BlockPos pos,Direction side,boolean isLeaving){
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
                    TileEntity te = world.getBlockEntity(acPos);
                    if (te instanceof PanelTile){
                        PanelTile panelTile = (PanelTile) te;
                        handler = (ICounterHandler) panelTile.getIPanelCell(PanelCellPos.fromIndex(panelTile,index));
                    }else {
                        throw new IllegalStateException("Index is not -1 and we get a tile entity which is not a panel tile !");
                    }
                }
                assert handler != null;
                if (addAxle){
                    handler.addAxle();
                    // we add or remove axle server side => need to add or remove axle client side as well
                    Networking.INSTANCE.send(PacketDistributor.ALL.noArg(), new SetAxleNumberPacket(acPos, 1,index));
                }else {
                    handler.removeAxle();
                    Networking.INSTANCE.send(PacketDistributor.ALL.noArg(), new SetAxleNumberPacket(acPos, -1,index));
                }
            });
        }
    }

    default void onMinecartPass(World world, BlockPos pos, AbstractMinecartEntity cart, RailShape shape){
        WorldCounterPoints worldCP = Util.getWorldCounterPoint(world);
        boolean isMinecartComing = isMinecartComing(worldCP,pos,cart);
        if (!isMinecartComing){
            boolean isMinecartLeaving = isMinecartLeaving(worldCP,pos,cart);
            if (isMinecartLeaving) {
                System.out.println("Minecart "+cart.getUUID()+ " is leaving block !");
                Direction motionDirection = getMotionDirection(cart,true,shape);
                onMinecartLimitPass(world,worldCP,pos,motionDirection,true);
            }
        }else {
            System.out.println("Minecart "+cart.getUUID()+" is coming on block !");
            Direction motionDirection = getMotionDirection(cart,false,shape);
            onMinecartLimitPass(world,worldCP,pos,motionDirection.getOpposite(),false);
        }
    }

    default void removeCP(World world,BlockState oldState,BlockState actualState,BlockPos cpPos){
        // this function is done only server side
        if (actualState.getBlock() != oldState.getBlock()) {
            WorldCounterPoints worldCP = Util.getWorldCounterPoint(world);
            worldCP.onCPBlockRemove(cpPos);
        }
    }

}
