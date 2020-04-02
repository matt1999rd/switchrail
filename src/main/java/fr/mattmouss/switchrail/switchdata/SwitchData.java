package fr.mattmouss.switchrail.switchdata;

import fr.mattmouss.switchrail.SwitchRailMod;
import fr.mattmouss.switchrail.blocks.SwitchTile;
import fr.mattmouss.switchrail.enum_rail.Corners;
import fr.mattmouss.switchrail.enum_rail.SwitchType;
import fr.mattmouss.switchrail.enum_rail.Tjd_Position;
import fr.mattmouss.switchrail.switchblock.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class SwitchData {

    public SwitchType type;
    public BlockPos pos;
    private static ResourceLocation SW_ST_GUI = new ResourceLocation(SwitchRailMod.MODID, "textures/gui/sw_straight_button.png");
    private static ResourceLocation SW_ST_TRN_GUI = new ResourceLocation(SwitchRailMod.MODID, "textures/gui/sw_straight_turn_button.png");
    private static ResourceLocation SW_FC_GUI = new ResourceLocation(SwitchRailMod.MODID, "textures/gui/sw_facing_button.png");
    private static ResourceLocation SW_FC_TRN_GUI = new ResourceLocation(SwitchRailMod.MODID, "textures/gui/sw_facing_turn_button.png");
    private static ResourceLocation SW_TJD_GUI = new ResourceLocation(SwitchRailMod.MODID, "textures/gui/sw_tjd_button.png");
    private static ResourceLocation SW_TRIPLE_GUI = new ResourceLocation(SwitchRailMod.MODID, "textures/gui/sw_triple_button.png");




    public SwitchData(SwitchType type_in, BlockPos pos_in) {
        type =type_in;
        pos = pos_in;
    }

    public SwitchData(SwitchTile tile){
        type = ((Switch)tile.getBlockState().getBlock()).getType();
        pos = tile.getPos();
    }




    // 6 picture are containing the icons for each possibilities in the gui display
    /**
     * Picture 1 (8): SW_ST_GUI contains all the scale of straight put switch_straight order from NORTH SwitchNLeft to EAST SwitchVRight
     *      SNL N, SNL E, SNR N, SNR E, SVL N, SVL E, SVR N, SVR E.
     * Picture 2 (8): SW_ST_TURN_GUI contains all the scale of turn put switch_straight with same order as previously
     * Picture 3 (8): SW_FC_GUI contains all the scale of double_turn and tjs switch order from NORTH switch_double_turn to WEST switch_tjs
     *      SDT N, SDT E, SDT S, SDT W, STJS N, STJS E, STJS S, STJS W
     * Picture 4 (8): SW_FC_TURN_GUI contains all the scale of double turn and tjs switch with turn corner and turn right corner properties with
     * same order as previously
     * Picture 5 (8): SW_TJD_GUI contains all the scale of TJD switch whatever switch_position or facing_axe it has with this order
     *      NP N, NP E, POLD N, POLD E, PORU N, PORU E, AP N, AP E
     * Picture 6 (12): SW_TRIPLE contains all the scale of Triple switch whatever switch_position or facing it has with this order
     *      TL N, TL E, TL S, TL W, S N, S E, S S, S W, TR N, TR E, TR S, TR W

     **/
    public ResourceLocation getResourceLocation(TileEntity te) {
        BlockState bs = te.getWorld().getBlockState(this.pos);
        if (bs.getBlock() instanceof Switch_Straight) {
            boolean isTurn = (bs.get(Switch_Straight.SWITCH_POSITION) == Corners.TURN);
            return (!isTurn) ? SW_ST_GUI : SW_ST_TRN_GUI;
        } else if (type.isDoubleSwitch()) {
            if (bs.getBlock() instanceof SwitchDoubleTurn){
                boolean isTurnLeft = (bs.get(SwitchDoubleTurn.SWITCH_POSITION) == Corners.TURN_LEFT);
                return (isTurnLeft) ? SW_FC_GUI : SW_FC_TRN_GUI;
            } else {
                boolean isTurn = (bs.get(Switch_Tjs.SWITCH_POSITION) == Corners.TURN);
                return (!isTurn) ? SW_FC_GUI : SW_FC_TRN_GUI;
            }
        }else if (type == SwitchType.TRIPLE) {
            return SW_TRIPLE_GUI;
        } else if (type == SwitchType.TJD) {
            return SW_TJD_GUI;
        } else {
            throw new NullPointerException("the switchtype type is actually null");
        }
    }

    private boolean isDoubleSwitch() {
        return type.isDoubleSwitch();
    }

    //how facing properties impact the placement on the picture
    public int getIntRelativeToFacing(TileEntity tileEntity){
        BlockState bs = tileEntity.getWorld().getBlockState(this.pos);
        if (bs.has(BlockStateProperties.HORIZONTAL_FACING)) {
            Direction d = bs.get(BlockStateProperties.HORIZONTAL_FACING);
            switch (d) {
                case NORTH:
                    return 0;
                case EAST:
                    return 1;
                case SOUTH:
                    return 2;
                case WEST:
                    return 3;
                case UP:
                case DOWN:
                default:
                    throw new IllegalArgumentException("No such Facing properties available in this type : "+bs.getBlock().getClass());
            }
        } else if (bs.has(Switch_Tjd.FACING_AXE)){
            Direction d = bs.get(Switch_Tjd.FACING_AXE);
            return (d==Direction.NORTH) ? 0 : 1;
        } else {
                RailShape shape = bs.get(Switch.RAIL_STRAIGHT_FLAT);
                return (shape == RailShape.NORTH_SOUTH) ? 0 : 1;
        }
    }

    //how type influence the placement on picture for common picture
    public int getIntRelativeToSwitchType(){
        if (this.isDoubleSwitch()){
            switch (type){
                case DOUBLE_TURN:
                case ST_NL:
                    return 0;
                case TJS:
                case ST_NR:
                    return 1;
                case ST_VL:
                    return 2;
                case ST_VR:
                    return 3;
                default:
                    throw new IllegalArgumentException("No such argument are acceptable for type : "+this.type);
            }
        }else{
            throw new IllegalArgumentException("No such argument are acceptable for type : "+this.type);
        }
    }

    //when corners are changing within the same picture we use an int to make sure it works well
    public int getIntRelativeToCorners(TileEntity te){
        BlockState bs = te.getWorld().getBlockState(this.pos);
        if (this.type == SwitchType.TRIPLE){
            Corners c = bs.get(Switch.SWITCH_POSITION_TRIPLE);
            switch (c){
                case TURN_LEFT:
                    return 0;
                case STRAIGHT:
                    return 1;
                case TURN_RIGHT:
                    return 2;
                default:
                    throw new IllegalArgumentException("No such Corners for triple switch : "+c);
            }
        }else if (this.type == SwitchType.TJD) {
            Tjd_Position t = bs.get(Switch_Tjd.SWITCH_POSITION);
            switch (t){
                case NO_POWER:
                    return 0;
                case POWER_ON_LEFT_DOWN:
                    return 1;
                case POWER_ON_RIGHT_UP:
                    return 2;
                case POWER_ON_BOTH:
                    return 3;
                default:
                    throw new IllegalArgumentException("No such TjdPosition available : "+t);
            }

        }else {
            throw new IllegalArgumentException("This function is not applicable to the given type : "+type);
        }


    }

    public int getXDebutImg(int tailleX,TileEntity te,int scaleY) {
        int ind = getIndScaleX(te);
        int ind_scaleY = 11 - scaleY;
        if (ind_scaleY>6){
            ind =ind % 6; //les deux dernières lignes sont enchevetre
        }
        return ind * tailleX;

    }

    public int getYDebutImg(int scaleY,TileEntity te) {
        int ind = 11 - scaleY;
        int ind_scaleX =getIndScaleX(te);

        int[] scaleYList = {0,9,18,29,41,55,71,90,114,138,171}; //cumul des scaleY
        if (ind<7) {
             //pour les premiers élements pas de superposition
        }else if (ind == 7){ //7 et 8 on effectue une division euclidienne qui va determiner la scaleY
            ind =MathHelper.intFloorDiv(ind_scaleX,6)+ind;
        }else if (ind == 8){
            System.out.println(ind_scaleX);
            System.out.println(MathHelper.intFloorDiv(ind_scaleX,6));
            ind=MathHelper.intFloorDiv(ind_scaleX,6)+ind+1;
        }else {
            throw new IllegalArgumentException("no such indice are allowed here : "+ind);
        }
        return scaleYList[ind];
    }

    private int getIndScaleX(TileEntity te){
        Block block =te.getWorld().getBlockState(this.pos).getBlock();
        int ind;
        if (block instanceof Switch_Straight){
            ind=4*getIntRelativeToFacing(te)+getIntRelativeToSwitchType();
        }else if (type.isDoubleSwitch()){
            ind=getIntRelativeToFacing(te)+4*getIntRelativeToSwitchType();
        }else if (block instanceof Switch_Tjd){
            ind=2*getIntRelativeToCorners(te)+getIntRelativeToFacing(te);
        }else if (block instanceof SwitchTriple){
            ind=4*getIntRelativeToCorners(te)+getIntRelativeToFacing(te);
        }else {
            throw new IllegalArgumentException("No other block authorized");
        }
        return ind;

    }


}
