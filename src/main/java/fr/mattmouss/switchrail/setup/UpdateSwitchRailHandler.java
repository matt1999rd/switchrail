package fr.mattmouss.switchrail.setup;

import fr.mattmouss.switchrail.blocks.ModBlock;
import fr.mattmouss.switchrail.enum_rail.Corners;
import fr.mattmouss.switchrail.enum_rail.Dss_Position;
import fr.mattmouss.switchrail.switchblock.Switch;
import fr.mattmouss.switchrail.switchblock.SwitchStraight;
import fr.mattmouss.switchrail.switchblock.SwitchDoubleSlip;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UpdateSwitchRailHandler {
    public static class WorldLoadingHandler {

        public WorldLoadingHandler() {
        }

        @SubscribeEvent
        public void onLoadChunk(ChunkEvent.Load event) {
            Chunk chunk = (Chunk) event.getChunk();
            chunk.getBlockEntitiesPos().stream().filter(blockPos->{
                String path = Objects.requireNonNull(chunk.getBlockState(blockPos).getBlock().getRegistryName()).getPath();
                return path.matches("switch_([nv])_(left|right)") || path.equals("switch_tjd");
            }).forEach(blockPos -> doUpdateOnBlock(Objects.requireNonNull(chunk.getWorldForge()),blockPos));
        }

        private void doUpdateOnBlock(World world,BlockPos pos){
            BlockState oldState = world.getBlockState(pos);
            BlockState newState;
            System.out.println("replacing block in position : "+pos);
            if (oldState.getBlock() instanceof SwitchStraight){
                newState = replaceBlock(oldState);
            }else {
                newState = changeState(oldState);
            }
            world.setBlock(pos,newState,11);
        }

        private BlockState replaceBlock(BlockState oldState){
            System.out.println("block state of the block to replace : "+oldState);
            BlockState newState = ModBlock.SWITCH_STRAIGHT
                    .defaultBlockState()
                    .setValue(SwitchStraight.SWITCH_POSITION_STANDARD,oldState.getValue(SwitchStraight.SWITCH_POSITION_STANDARD));
            String[] paths = Objects.requireNonNull(oldState.getBlock().getRegistryName()).getPath().split("_");
            RailShape oldStateShape = oldState.getValue(SwitchStraight.RAIL_STRAIGHT_FLAT);
            Direction facing =  getFacing(Objects.equals(paths[1], "v"),oldStateShape==RailShape.NORTH_SOUTH);
            DoorHingeSide side = getHinge(Objects.equals(paths[1], "v"), Objects.equals(paths[2], "left"));
            return newState.setValue(BlockStateProperties.HORIZONTAL_FACING,facing).setValue(BlockStateProperties.DOOR_HINGE,side);
        }

        private Direction getFacing(boolean isVSwitch,boolean isShapeNorthSouth){
            int index = 0;
            if (isVSwitch)index+=2;
            if (!isShapeNorthSouth)index+=1;
            return Direction.from2DDataValue(index);
        }

        private DoorHingeSide getHinge(boolean isVSwitch,boolean isLeftSwitch){
            return (isLeftSwitch == isVSwitch) ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
        }

        //replace block state property of the double slip switch for the switch position
        // NO POWER -> STRAIGHT
        // POWER_ON_LEFT_DOWN -> TURN_LEFT
        // POWER_ON_RIGHT_UP -> TURN_RIGHT
        // POWER_ON_BOTH -> TURN
        private BlockState changeState(BlockState oldState){
            Dss_Position position = oldState.getValue(SwitchDoubleSlip.SWITCH_POSITION_OLD);
            Direction dir = oldState.getValue(SwitchDoubleSlip.FACING_AXE);
            Corners equivalentValue = Corners.getCorner(position);
            assert equivalentValue != null;
            System.out.println("done !");
            return oldState.setValue(Switch.DSS_POSITION,equivalentValue).setValue(BlockStateProperties.HORIZONTAL_AXIS,dir.getAxis());
        }

    }
}
