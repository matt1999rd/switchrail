package fr.mattmouss.switchrail.enum_rail;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.mattmouss.switchrail.blocks.AxleCounterRail;
import fr.mattmouss.switchrail.blocks.ControllerBlock;
import fr.mattmouss.switchrail.blocks.CrossedRail;
import fr.mattmouss.switchrail.other.Util;
import fr.mattmouss.switchrail.switchblock.*;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec2;

public enum RailType {
    CROSSED_RAIL(10,CrossedRail.class),
    AXLE_COUNTER(0, AxleCounterRail.class),
    CONTROLLER_BLOCK(11,ControllerBlock.class),
    SIMPLE(12, SwitchStraight.class),
    Y(28,SwitchDoubleTurn.class),
    THREE_WAY(36,SwitchTriple.class),
    SINGLE_SLIP(48, SwitchSimpleSlip.class),
    DOUBLE_SLIP(56, SwitchDoubleSlip.class),
    RAIL(0,BaseRailBlock.class);

    final int shift;
    final Class<? extends Block> instanceClass;
    final Vec2 uvDimension = Util.makeVector(32F/256F);


    RailType(int shift, Class<? extends Block> instanceClass){
        this.shift = shift;
        this.instanceClass = instanceClass;
    }

    public static RailType getType(Block block){
        for (RailType switchType : RailType.values()){
            Class<?> class_in = block.getClass();
            if (switchType == RAIL){
                if (BaseRailBlock.class.isAssignableFrom(class_in)){
                    return switchType;
                }
            }else if (class_in == switchType.instanceClass) {
                return switchType;
            }
        }
        return null;
    }

    public boolean isSwitch(){
        return this != CROSSED_RAIL && this != CONTROLLER_BLOCK && this != RAIL && this != AXLE_COUNTER;
    }

    public boolean canContainCP(){
        return this.isSwitch() || this == AXLE_COUNTER;
    }

    public boolean isNormalRail(){
        return this == RAIL || this == AXLE_COUNTER;
    }

    // it returns the possible direction where there is no need to add an arrow in counter screen

    public Direction getUnusedDirection(BlockState state){
        // if all direction are used, it returns null (triple switch and switch double or single slip)
        if (state.getBlock() instanceof SwitchDoubleSlip || state.getBlock() instanceof SwitchSimpleSlip || state.getBlock() instanceof SwitchTriple){
            return null;
        }
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        // facing in switch double turn correspond to the only side where no rail is heading
        if (state.getBlock() instanceof SwitchDoubleTurn){
            return facing;
        }
        // facing in switch straight correspond to the straight rail on trail-facing part
        // therefore the free part is on the right if hinge is left (clockwise) and on the left if hinge is right (counterclockwise)
        if (state.getBlock() instanceof SwitchStraight){
            DoorHingeSide doorSide = state.getValue(BlockStateProperties.DOOR_HINGE);
            return (doorSide == DoorHingeSide.LEFT)? facing.getClockWise() : facing.getCounterClockWise();
        }
        throw new IllegalStateException("Expect a switch in this function as blockstate ! This block was given :"+state.getBlock());
    }

    // return the UV Shift in this order :
    // 0  1  2  3  4  5  6  7
    // 8  9  10 11 12 13 14 15
    // 16 17 18 19 20 21 22 23
    // 24 25 26 27 28 29 30 31
    // 32 33 34 35 36 37 38 39
    // 40 41 42 43 44 45 46 47
    // 48 49 50 51 52 53 54 55
    // 56 57 58 59 60 61 62 63
    public int getUVShift(BlockState state){
        int bs_shift = 0;
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && this != CONTROLLER_BLOCK){
            Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            bs_shift+=dir.get2DDataValue();
        }
        if (this.isNormalRail()){
            EnumProperty<RailShape> properties = (EnumProperty<RailShape>) state.getProperties().stream().filter(property -> property.getValueClass() == RailShape.class).findAny().get();
            RailShape shape = state.getValue(properties);
            bs_shift+=shape.ordinal();
        }
        if (state.hasProperty(BlockStateProperties.DOOR_HINGE)){
            DoorHingeSide side = state.getValue(BlockStateProperties.DOOR_HINGE);
            bs_shift+=side.ordinal()*8;
        }
        if (this == DOUBLE_SLIP){
            if (!(state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS))){
                throw new IllegalStateException(
                        "Type called is not matching the block state given in argument : expect tjd got "
                                +state.getBlock().getRegistryName()+" instead" );
            }
            Direction.Axis dir_axis = state.getValue(BlockStateProperties.HORIZONTAL_AXIS);
            // when replacing axis we have to map the NORTH part to the Z axis and the EAST part to the X axis
            // this leads to use the ordinal and map 0 to 1 and 2 to 0 -> y=ax+b
            // y(0) = 1 -> b = 1 ; y(2) = 0 -> 2a + 1 = 0 -> a = -1/2
            // y = 1 - x/2
            bs_shift+=1-dir_axis.ordinal()/2;
        }

        EnumProperty<Corners> cornersProperty = getCornerProperty(state);
        if (cornersProperty != null){
            Object[] possibleCorner = cornersProperty.getPossibleValues().stream().sorted().toArray();
            for (int i=0;i<possibleCorner.length;i++){
                if (possibleCorner[i] == state.getValue(cornersProperty)){
                    // include dss in this condition forces to have an intern condition on the possible value
                    // only dss has the four possibilities
                    bs_shift += i*(possibleCorner.length == 4 ? 2 : 4);
                }
            }
        }
        return bs_shift+this.shift;
    }

    private static EnumProperty<Corners> getCornerProperty(BlockState state){
        if (state.getBlock() instanceof Switch){
            return ((Switch)state.getBlock()).getSwitchPositionProperty();
        }
        return null;
    }

    public void render(PoseStack stack, Vec2 posOnBoard, Vec2 iconDimension, BlockState blockState){
        int uvShift = this.getUVShift(blockState);
        Vec2 uvOrigin = Util.directMult(new Vec2(uvShift%8,(float)(uvShift/8)),uvDimension);
        Util.renderQuad(stack,posOnBoard, Util.add(posOnBoard,iconDimension),uvOrigin,Util.add(uvOrigin,uvDimension));
    }

}
