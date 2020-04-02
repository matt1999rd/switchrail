package fr.mattmouss.switchrail.blocks;

import fr.mattmouss.switchrail.gui.ControllerContainer;
import fr.mattmouss.switchrail.switchblock.*;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class ModBlock {

    @ObjectHolder("switchrail:switch_v_right")

    public static Switch_Straight switchStraightVRight = new SwitchStraightVRight();

    @ObjectHolder("switchrail:switch")

    public static TileEntityType<SwitchTile> SWITCH_TILE;

    @ObjectHolder("switchrail:switch_v_left")

    public static Switch_Straight switchStraightVLeft = new SwitchStraightVLeft();

    @ObjectHolder("switchrail:switch_n_left")

    public static Switch_Straight switchStraightNLeft = new SwitchStraightNLeft();

    @ObjectHolder("switchrail:switch_n_right")

    public static Switch_Straight switchStraightNRight = new SwitchStraightNRight();

    @ObjectHolder("switchrail:double_turn_switch")

    public static SwitchDoubleTurn SWITCH_DOUBLE_TURN = new SwitchDoubleTurn();

    @ObjectHolder("switchrail:crossed_rail")

    public static CrossedRail CROSSED_RAIL = new CrossedRail();

    @ObjectHolder("switchrail:triple_switch")

    public static SwitchTriple TRIPLE_SWITCH = new SwitchTriple();

    @ObjectHolder("switchrail:switch_tjs")

    public static Switch_Tjs SWITCH_TJS = new Switch_Tjs();

    @ObjectHolder("switchrail:switch_tjd")

    public static Switch_Tjd SWITCH_TJD = new Switch_Tjd();

    @ObjectHolder("switchrail:controller_block")

    public static ControllerBlock CONTROLLER_BLOCK = new ControllerBlock();

    @ObjectHolder("switchrail:controller_block")

    public static TileEntityType<ControllerTile> CONTROLLER_TILE;

    @ObjectHolder("switchrail:controller_block")

    public static ContainerType<ControllerContainer> CONTROLLER_CONTAINER;

    @ObjectHolder("switchrail:bumper")

    public static Bumper BUMPER = new Bumper();

    @ObjectHolder("switchrail:bumper")

    public static TileEntityType<BumperTile> BUMPER_TILE;

    @ObjectHolder("switchrail:one_way_powered_rail")

    public static  OneWayPoweredRail ONE_WAY_POWERED_RAIL = new OneWayPoweredRail();











}
