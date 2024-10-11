package fr.moonshade.switchrail.blocks;

import fr.moonshade.switchrail.switchblock.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class ModBlock {
    @ObjectHolder("switchrail:switch_straight")

    public static SwitchStraight SWITCH_STRAIGHT = new SwitchStraight();

    @ObjectHolder("switchrail:double_turn_switch")

    public static SwitchDoubleTurn SWITCH_DOUBLE_TURN = new SwitchDoubleTurn();

    @ObjectHolder("switchrail:crossed_rail")

    public static CrossedRail CROSSED_RAIL = new CrossedRail();

    @ObjectHolder("switchrail:triple_switch")

    public static SwitchTriple TRIPLE_SWITCH = new SwitchTriple();

    @ObjectHolder("switchrail:switch_tjs")

    public static SwitchSimpleSlip SWITCH_TJS = new SwitchSimpleSlip();

    @ObjectHolder("switchrail:switch_tjd")

    public static SwitchDoubleSlip SWITCH_TJD = new SwitchDoubleSlip();

    @ObjectHolder("switchrail:controller_block")

    public static ControllerBlock CONTROLLER_BLOCK = new ControllerBlock();

    @ObjectHolder("switchrail:controller_block")

    public static BlockEntityType<ControllerTile> CONTROLLER_TILE;

    @ObjectHolder("switchrail:bumper")

    public static Bumper BUMPER = new Bumper();

    @ObjectHolder("switchrail:bumper")

    public static BlockEntityType<BumperTile> BUMPER_TILE;

    @ObjectHolder("switchrail:one_way_powered_rail")

    public static  OneWayPoweredRail ONE_WAY_POWERED_RAIL = new OneWayPoweredRail();

    @ObjectHolder("switchrail:one_way_detector_rail")

    public static OneWayDetectorRail ONE_WAY_DETECTOR_RAIL = new OneWayDetectorRail();

    @ObjectHolder("switchrail:switch_terminal")

    public static SwitchTerminal SWITCH_TERMINAL = new SwitchTerminal();

    @ObjectHolder("switchrail:switch_terminal")

    public static BlockEntityType<TerminalTile> TERMINAL_TILE;

    @ObjectHolder("switchrail:axle_counter_point")

    public static AxleCounterPoint AXLE_COUNTER = new AxleCounterPoint();

    @ObjectHolder("switchrail:axle_counter_rail")

    public static AxleCounterRail AXLE_COUNTER_RAIL = new AxleCounterRail();

    @ObjectHolder("switchrail:axle_counter_point")

    public static BlockEntityType<AxleCounterTile> AXLE_COUNTER_TILE;











}
