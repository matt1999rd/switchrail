package fr.mattmouss.switchrail.blocks;

import fr.mattmouss.switchrail.switchblock.*;
import net.minecraft.tileentity.TileEntityType;
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

    public static TileEntityType<ControllerTile> CONTROLLER_TILE;

    @ObjectHolder("switchrail:bumper")

    public static Bumper BUMPER = new Bumper();

    @ObjectHolder("switchrail:bumper")

    public static TileEntityType<BumperTile> BUMPER_TILE;

    @ObjectHolder("switchrail:one_way_powered_rail")

    public static  OneWayPoweredRail ONE_WAY_POWERED_RAIL = new OneWayPoweredRail();

    @ObjectHolder("switchrail:one_way_detector_rail")

    public static OneWayDetectorRail ONE_WAY_DETECTOR_RAIL = new OneWayDetectorRail();

    @ObjectHolder("switchrail:switch_terminal")

    public static SwitchTerminal SWITCH_TERMINAL = new SwitchTerminal();

    @ObjectHolder("switchrail:switch_terminal")

    public static TileEntityType<TerminalTile> TERMINAL_TILE;











}
