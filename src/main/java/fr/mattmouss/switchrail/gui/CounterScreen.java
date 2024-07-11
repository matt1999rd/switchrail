package fr.mattmouss.switchrail.gui;

import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import fr.mattmouss.switchrail.SwitchRailMod;
import fr.mattmouss.switchrail.axle_point.CPFlag;
import fr.mattmouss.switchrail.axle_point.CounterPoint;
import fr.mattmouss.switchrail.axle_point.CounterPointInfo;
import fr.mattmouss.switchrail.axle_point.WorldCounterPoints;
import fr.mattmouss.switchrail.blocks.*;
import fr.mattmouss.switchrail.enum_rail.RailType;
import fr.mattmouss.switchrail.network.Networking;
import fr.mattmouss.switchrail.network.SetAxleNumberPacket;
import fr.mattmouss.switchrail.network.UpdateCounterPointPacket;
import fr.mattmouss.switchrail.other.Util;
import fr.mattmouss.switchrail.other.Vector2i;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec2;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;


public class CounterScreen extends RailScreen{

    private static final ResourceLocation COUNTER_ICONS = new ResourceLocation(SwitchRailMod.MOD_ID,"textures/gui/counter_icons.png");
    private static final int offsetV = 52;
    private static final int arrowLongLength = 16;
    private static final int arrowShortLength = 8;
    private static final int textureDimension = 256;
    private static final int iconPxLength = 32;
    private static final Vector2i additionalScreenDimension = new Vector2i(76,26);
    private static final Vec2 arrowUVOrigin = new Vec2(0, (offsetV + additionalScreenDimension.y) /256F);
    private static final int AXLE_SCREEN_X_BEGINNING = WIDTH - 2;
    private static final int AXLE_SCREEN_Y_BEGINNING = HEIGHT - additionalScreenDimension.y;
    private ImageButton removeAxleButton;
    private final CounterPointInfo cpInfo;
    private final int index; // -1 if no index is given. Bad usage to my mind but optional is forbidden

    public CounterScreen(BlockPos pos, CounterPointInfo cpInfo,int index) {
        super(pos);
        this.cpInfo = cpInfo;
        this.index = index;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected ICounterHandler getHandler() {
        assert this.minecraft != null;
        assert this.minecraft.level != null;
        BlockEntity tileEntity = this.minecraft.level.getBlockEntity(pos);
        if (tileEntity instanceof AxleCounterTile){
            return (ICounterHandler) tileEntity;
        }else if (tileEntity instanceof PanelTile) {
            PanelTile panelTile = (PanelTile) tileEntity;
            if (index == -1) throw new IllegalStateException("Try to open a screen for a tiny switch terminal while no cell position on the panel was given from the server");
            PanelCellPos cellPos = PanelCellPos.fromIndex(panelTile,index);
            IPanelCell panelCell = cellPos.getPanelTile().getIPanelCell(cellPos); // There is a strange bug here : the panel cell changes between init function and this function call
            if (panelCell instanceof AxleCounterPointCell){
                return (ICounterHandler) panelCell;
            }else {
                throw new IllegalStateException("Found a tiny block that is not an axle counter. It is a cell of type : "+ (panelCell == null ? "null" : panelCell.getClass()));
            }
        }
        String teType = (tileEntity == null) ? "null" : String.valueOf(tileEntity.getClass());
        throw new IllegalStateException("BlockPos of the axle counter screen ("+pos+") is not associated with a correct tile entity (found tile entity of type "+teType+" ) !");
    }

    @Override
    protected boolean isRelevantRail(RailType type) {
        return type.canContainCP();
    }

    @Override
    protected ResourceLocation getIcon() {
        return COUNTER_ICONS;
    }

    @Override
    public void init() {
        super.init();
        Vector2i relative = getRelative();
        int buttonLength = 16; //lengths of buttons
        int button0Height = 13;
        int buttonPMHeight = 10;
        int xText = additionalScreenDimension.x;
        int yText = offsetV; //position y in the texture
        int offsetXPMBtn = 39;
        int offsetX0Btn = 55;
        int offsetYPMBtn = 3;
        int offsetY0Btn = 7;

        ImageButton addAxleButton = new ImageButton(relative.x + AXLE_SCREEN_X_BEGINNING + offsetXPMBtn,
                relative.y + AXLE_SCREEN_Y_BEGINNING + offsetYPMBtn,
                buttonLength,buttonPMHeight,
                xText,yText,buttonPMHeight, // UV mapping and V value if mouse is hovering
                POS_BUTTON, button -> addAxle());
        this.removeAxleButton = new ImageButton(relative.x + AXLE_SCREEN_X_BEGINNING + offsetXPMBtn,
                relative.y + AXLE_SCREEN_Y_BEGINNING + offsetYPMBtn + buttonPMHeight,
                buttonLength,buttonPMHeight,
                xText+buttonLength,yText,buttonPMHeight,
                POS_BUTTON, button -> removeAxle());
        Button freeButton = new ImageButton(relative.x + AXLE_SCREEN_X_BEGINNING + offsetX0Btn,
                relative.y + AXLE_SCREEN_Y_BEGINNING + offsetY0Btn,
                buttonLength,button0Height,
                xText+2*buttonLength,yText,button0Height,
                POS_BUTTON, button -> freePoint());
        ICounterHandler handler = getHandler();
        if (handler.isFree()){
            removeAxleButton.visible = false;
        }
        addRenderableWidget(addAxleButton);
        addRenderableWidget(removeAxleButton);
        addRenderableWidget(freeButton);
    }

    private void addAxle(){
        ICounterHandler handler = getHandler();
        handler.addAxle();
        if (!removeAxleButton.visible) removeAxleButton.visible = true;
        Networking.INSTANCE.sendToServer(new SetAxleNumberPacket(this.pos,1,index));
    }

    private void removeAxle(){
        ICounterHandler handler = getHandler();
        handler.removeAxle();
        if (handler.isFree())removeAxleButton.visible = false;
        Networking.INSTANCE.sendToServer(new SetAxleNumberPacket(this.pos,-1,index));
    }

    private void freePoint(){
        ICounterHandler handler = getHandler();
        handler.freePoint();
        removeAxleButton.visible = false;
        Networking.INSTANCE.sendToServer(new SetAxleNumberPacket(this.pos,0,index));
    }

    @Override
    @ParametersAreNonnullByDefault
    public void renderBackground(PoseStack stack) {
        super.renderBackground(stack);
        Vector2i relative = getRelative();
        assert this.minecraft != null;
        RenderSystem.setShaderTexture(0,POS_BUTTON);
        this.blit(stack,relative.x+AXLE_SCREEN_X_BEGINNING,relative.y+AXLE_SCREEN_Y_BEGINNING,
                0,offsetV,additionalScreenDimension.x,additionalScreenDimension.y);
        ICounterHandler handler = getHandler();
        int axleNumber = handler.getAxle();
        drawString(stack,axleNumber+"",
                AXLE_SCREEN_X_BEGINNING + 10,
                AXLE_SCREEN_Y_BEGINNING + 10,
                Color.WHITE);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        Vector2i relative = getRelative();
        super.render(stack, mouseX, mouseY, partialTicks);
        Map<BlockPos,RailType> blockWithCPOnBoard = getBlockOnBoard(false);
        // we will render all limits arrow of the switch or ACR
        blockWithCPOnBoard.forEach((blockPos,railType) -> {
            BlockState state = getBlockState(blockPos);
            // this predicates indicates if the direction need an arrow rendering
            Predicate<Direction> testDirection = dir -> railType.isSwitch() ? dir == railType.getUnusedDirection(state) :
                    !Util.test(state.getValue(BlockStateProperties.RAIL_SHAPE),dir);
            for (int i = 0; i < 4; i++) {
                Direction dir = Direction.from2DDataValue(i);
                if (testDirection.test(dir)) continue;
                byte cpInfo = this.cpInfo.getCounterPointByte(blockPos, dir);
                if (cpInfo == -1) { // point is not activated
                    renderArrow(stack, blockPos, dir, false, false, false, false,relative);
                } else { //point is activated
                    boolean fromOutside = CounterPointInfo.readFlag(cpInfo, CPFlag.FROM_OUTSIDE);
                    boolean addAxle = CounterPointInfo.readFlag(cpInfo, CPFlag.ADD_AXLE);
                    boolean bidirectional = CounterPointInfo.readFlag(cpInfo,CPFlag.BIDIRECTIONAL);
                    renderArrow(stack, blockPos, dir, fromOutside, addAxle, bidirectional, true, relative);
                }
            }

        });
        renderExplanation(stack,mouseX,mouseY,relative);
    }

    //isActivated indicates if we must count this limit in the axle counter
    private void renderArrow(PoseStack stack, BlockPos pos, Direction side, boolean fromOutside,boolean addAxle,boolean bidirectional, boolean isActivated,Vector2i relative){
        assert this.minecraft != null;
        RenderSystem.setShaderTexture(0,POS_BUTTON);

        if (isOnBoard(pos)){
            // the origin for the rendering
            Vec2 posOnBoard = getPosOnBoard(pos,relative);
            // dimension of the block
            Vec2 iconDimension = getDimensionOnBoard();
            //dimension of the arrow on Board
            Vec2 arrowDimensionOnBoard = getArrowDimension(side,Util.scale(iconDimension,1F/iconPxLength));
            // this offset is used to place the arrow on the square of the block
            Vec2 arrowOrigin = getArrowOrigin(side,posOnBoard,iconDimension,arrowDimensionOnBoard);
            // this arrowDimension is indicating what place the arrow will take on UV (for final GUI, it needs a scaling operation)
            Vec2 arrowDimension = getArrowDimension(side,Util.makeVector(1F/textureDimension));
            // the origin for the UV placement. If fromOutside is true, we reverse the side to consider
            Vec2 uvOrigin = getUVOrigin(fromOutside?side.getOpposite():side,isActivated,addAxle,bidirectional);
            // final rendering
            Util.renderQuad(stack,arrowOrigin,Util.add(arrowOrigin,arrowDimensionOnBoard)
                    ,uvOrigin,Util.add(uvOrigin,arrowDimension),true);
        }
    }

    // render information when using large zoom (difficulty to see the arrow)
    private void renderExplanation(PoseStack stack,int mouseX,int mouseY,Vector2i relative){
        Pair<RailType,BlockPos> data = getSwitchClicked(mouseX,mouseY,relative);
        if (data != null){
            Direction side = getSideClicked(data.getSecond(),relative,mouseX,mouseY);
            if (cpInfo.containsKey(data.getSecond(),side)){
                byte b = cpInfo.getCounterPointByte(data.getSecond(),side);
                boolean addAxle = CounterPointInfo.readFlag(b,CPFlag.ADD_AXLE);
                boolean fromOutside = CounterPointInfo.readFlag(b,CPFlag.FROM_OUTSIDE);
                boolean bidirectional = CounterPointInfo.readFlag(b,CPFlag.BIDIRECTIONAL);
                String explanation = getExplanation(addAxle,fromOutside,side);
                if (!bidirectional){
                    renderTooltip(stack, Component.nullToEmpty(explanation),mouseX,mouseY);
                }else {
                    String reverseExplanation = getExplanation(!addAxle,!fromOutside,side);
                    renderTooltip(stack, Lists.newArrayList(Component.nullToEmpty(explanation), Component.nullToEmpty(reverseExplanation)), Optional.empty(),mouseX,mouseY,font);
                }
            }
        }
    }

    // get string of explanation
    private String getExplanation(boolean addAxle,boolean fromOutside,Direction side){
        return ((addAxle) ? "Add" : "Remove") + " Axle when cart is heading "+
                ((fromOutside)? side.getOpposite() : side).getName()+".";
    }

    // on GUI, the arrow is rendered on the limit of the block
    // rO : renderingOrigin
    // rD : renderingDimension
    // aDOB : arrowDimensionOnBoard
    // iPL : iconPxLength
    // aSL : arrowShortLength
    // starting from the center of the block (rO + rD/2), we go in the side direction half the total block length minus half the length of the arrow on GUI (normal2D)
    // Then we are at the center of the arrow, and we move half the dimension of the arrow on GUI to get to the arrow origin (-aDOB/2)

    private Vec2 getArrowOrigin(Direction side,Vec2 renderingOrigin,Vec2 renderingDimension,Vec2 arrowDimensionOnBoard){
        // scale = (iPL / 2 - aSL / 2) * pxLengthOnGUI pxLengthOnGUI = rD / iPL
        float scale = (1/2F - arrowShortLength/(2F *iconPxLength)) * (float) side.getAxis().choose(renderingDimension.x,0,renderingDimension.y);
        // scale * dirOffset = vector2D
        Vec2 normal2D = Util.scale(Vector2i.project2D(side.getNormal(), Direction.Axis.Y).toFloatVector(),scale);
        return Util.add(renderingOrigin,Util.scale(renderingDimension,1/2F),normal2D,Util.scale(arrowDimensionOnBoard,-1/2F));
    }

    private Vec2 getUVOrigin(Direction side,boolean isEnabled,boolean addAxle,boolean biDirectional){
        float dim = arrowShortLength*1.0F/textureDimension;
        int offsetV = getOffsetV(side,isEnabled,addAxle,biDirectional);
        Vec2 allArrowOrigin = Util.add(arrowUVOrigin,new Vec2(0,arrowLongLength*offsetV*1F/textureDimension));
        boolean needUOffset = needUOffset(side, addAxle, biDirectional);
        boolean needVOffset = needVOffset(side, addAxle, biDirectional);
        if (needUOffset){
            return Util.add(new Vec2(dim,0),allArrowOrigin);
        }else if (needVOffset){
            return Util.add(new Vec2(0,dim),allArrowOrigin);
        }else if (!Direction.Axis.Y.test(side)){
            return allArrowOrigin;
        }else {
            throw new IllegalStateException("Expect horizontal facing as side for arrow rendering. Find "+side+" instead.");
        }
    }

    //a half square offset is necessary in this condition :
    // if unidirectional : when we want to render the right part of the diamond ie arrow pointing to right side (EAST)
    // if bidirectional : the 1st half part is arrow red < > arrow green, the 2nd part is arrow green < > arrow red
    // 1st half occurs when we add axle when heading WEST or remove axle when heading EAST
    // 2nd half occurs when we add axle when heading EAST or remove axle when heading WEST
    private boolean needUOffset(Direction side,boolean addAxle,boolean biDirectional){
        if (!biDirectional){
            return side == Direction.EAST;
        }else if (Direction.Axis.X.test(side)){
            return (side == Direction.WEST) == addAxle;
        } else {
            return false;
        }
    }

    //a half square offset is necessary in this condition :
    // if unidirectional : when we want to render the down part of the diamond ie arrow pointing to downside (SOUTH)
    // if bidirectional : the 1st half part is arrow red ^ v arrow green, the 2nd part is arrow green v ^ arrow red
    // 1st half occurs when we add axle when heading SOUTH or remove axle when heading NORTH
    // 2nd half occurs when we add axle when heading NORTH or remove axle when heading SOUTH
    private boolean needVOffset(Direction side,boolean addAxle,boolean biDirectional){
        if (!biDirectional){
            return side == Direction.SOUTH;
        }else if (Direction.Axis.Z.test(side)){
            return (side == Direction.NORTH) == addAxle;
        }else {
            return false;
        }
    }

    // 5 slots are presents :
    // 1st slot : grey square ( for disabled counterpoint)
    // 2nd slot : a red diamond ( for unidirectional removing axle counterpoint)
    // 3rd slot : a green diamond ( for unidirectional adding axle counterpoint)
    // 4th slot : all 2 bidirectional arrow for EAST and WEST direction counterpoint
    // 5th slot : all 2 bidirectional arrow for NORTH and SOUTH direction counterpoint
    private int getOffsetV(Direction side,boolean isEnabled,boolean addAxle,boolean biDirectional){
        if (isEnabled){
            if (biDirectional){
                return Direction.Axis.Z.test(side) ? 4 : 3;
            }else {
                return addAxle ? 2 : 1;
            }
        }
        return 0;
    }

    private Vec2 getArrowDimension(Direction side,Vec2 scale){
        Vec2 unscaledDim = (side.getAxis() == Direction.Axis.X)?
                new Vec2(arrowShortLength,arrowLongLength) :
                new Vec2(arrowLongLength,arrowShortLength) ;
        return Util.directMult(unscaledDim,scale);
    }

    @Override
    public boolean onRailClicked(Pair<RailType, BlockPos> data, double mouseX, double mouseY, Vector2i relative, int button) {
        if (data.getFirst().canContainCP()){
            BlockPos cpPos = data.getSecond();
            BlockPos acPos = this.pos;
            Direction side = getSideClicked(cpPos,relative,mouseX,mouseY);
            boolean isCTRLDown = Screen.hasControlDown();
            boolean isShiftDown = Screen.hasShiftDown() && !isCTRLDown; // to avoid making double action when shift and ctrl is down
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT){
                if (!cpInfo.containsKey(cpPos,side)){
                    return false;
                }else {
                    if (isCTRLDown){
                        Networking.INSTANCE.sendToServer(new UpdateCounterPointPacket(cpPos, WorldCounterPoints.TOGGLE_COUNTING,null,acPos,side,index));
                        cpInfo.toggleCounting(cpPos,side);
                    }else if (isShiftDown) {
                        Networking.INSTANCE.sendToServer(new UpdateCounterPointPacket(cpPos,WorldCounterPoints.TOGGLE_BIDIRECTIONAL,null,acPos,side,index));
                        cpInfo.toggleBiDirectional(cpPos,side);
                    }else {
                        Networking.INSTANCE.sendToServer(new UpdateCounterPointPacket(cpPos,WorldCounterPoints.TOGGLE_DIRECTION,null,acPos,side,index));
                        cpInfo.toggleDirection(cpPos,side);
                    }
                    return true;
                }
            }else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT){
                if (!cpInfo.containsKey(cpPos,side)){
                    CounterPoint counterPoint = new CounterPoint(acPos,index,side,false,false,false);
                    Networking.INSTANCE.sendToServer(new UpdateCounterPointPacket(cpPos,WorldCounterPoints.ADD_COUNTER_PT,counterPoint,null,null,index));
                    cpInfo.addCounterPoint(cpPos,counterPoint);
                }else {
                    Networking.INSTANCE.sendToServer(new UpdateCounterPointPacket(cpPos,WorldCounterPoints.REMOVE_COUNTER_PT,null,acPos,side,index));
                    cpInfo.removeCounterPoint(cpPos,side);
                }
            }

        }
        return false;
    }

    private Direction getSideClicked(BlockPos cpBlockPos,Vector2i relative,double mouseX,double mouseY){
        boolean isMouseAbove45DescDiagonal = isMouseAbove45degDiagonal(cpBlockPos,true,relative,mouseX,mouseY);
        boolean isMouseAbove45AscDiagonal = isMouseAbove45degDiagonal(cpBlockPos,false,relative,mouseX,mouseY);
        if (isMouseAbove45DescDiagonal){
            return isMouseAbove45AscDiagonal ? Direction.NORTH : Direction.EAST;
        }else {
            return isMouseAbove45AscDiagonal ? Direction.WEST : Direction.SOUTH;
        }
    }

    @Override
    public boolean isDisabled(BlockPos pos) {
        return false;
    } // all switch are always enabled -> there is no reason to block detection functionality
}
