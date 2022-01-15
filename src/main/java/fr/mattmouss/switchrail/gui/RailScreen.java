package fr.mattmouss.switchrail.gui;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import fr.mattmouss.switchrail.SwitchRailMod;
import fr.mattmouss.switchrail.blocks.IPosBaseTileEntity;
import fr.mattmouss.switchrail.enum_rail.RailType;
import fr.mattmouss.switchrail.network.ChangePosPacket;
import fr.mattmouss.switchrail.network.Networking;
import fr.mattmouss.switchrail.other.Util;
import fr.mattmouss.switchrail.other.Vector2i;
import fr.mattmouss.switchrail.switchblock.SwitchDoubleSlip;
import fr.mattmouss.switchrail.switchdata.RailData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class RailScreen extends Screen implements IGuiEventListener {

    private final ResourceLocation GUI = new ResourceLocation(SwitchRailMod.MOD_ID, "textures/gui/controller_gui.png");
    protected final ResourceLocation POS_BUTTON = new ResourceLocation(SwitchRailMod.MOD_ID,"textures/gui/posbutton.png");
    private static final ResourceLocation ICONS = new ResourceLocation(SwitchRailMod.MOD_ID,"textures/gui/controller_icons.png");

    protected static final int WIDTH = 227;
    protected static final int HEIGHT = 175;
    private static final Vector2f internalGuiDimension = new Vector2f(13,22);
    private static final int internalGuiWidth = 146;
    private static final int internalGuiHeight = 99;
    private static final int endInternalGuiX = 161;

    private float decimalPartScaleY = 0.0F;

    private Button ZoomInButton;

    private Button ZoomOutButton;

    private final TextFieldWidget[] posTextFields = new TextFieldWidget[3];

    private final NumberTextField[] scaleTextFields = new NumberTextField[2];

    protected final BlockPos pos;

    public RailScreen(BlockPos pos) {
        super(ITextComponent.nullToEmpty("RailSystem"));
        this.pos = pos;
    }

    protected Vector2i getRelative(){
        return new Vector2i(
                 (this.width - WIDTH)   / 2,
                 (this.height - HEIGHT) / 2);
    }

    private boolean isZoomAuthorised(int sign){
        //Zoom+ : ZoomIn : sign = -1 : low limit of 1
        //Zoom- : ZoomOut : sign = +1 : above limit of 50
        int limit = (sign>0)? 50 : 1;
        return getScale(Direction.Axis.X) != limit;
    }


    public void init() {
        Vector2i relative = getRelative();

        Button doneButton = new Button(relative.x + 12,
                relative.y + 122,
                146,
                20,
                ITextComponent.nullToEmpty("Done"),
                button -> onClose());

        addButton(doneButton);

        Button setDefaultPosButton = new Button(relative.x + endInternalGuiX,
                relative.y + 106,
                60,
                20,
                ITextComponent.nullToEmpty("Reset Pos"),
                button -> {
                    for (Direction.Axis axis : Direction.Axis.values()){
                        changePos(pos.get(axis),axis);
                    }
                });

        addButton(setDefaultPosButton);

        ZoomInButton = new Button(relative.x + endInternalGuiX,
                relative.y + 26,
                50,
                20,
                ITextComponent.nullToEmpty("Zoom+"),
                button -> Zoom(-1));
        ZoomOutButton = new Button(relative.x + endInternalGuiX,
                relative.y + 47,
                50,
                20,
                ITextComponent.nullToEmpty("Zoom-"),
                button -> Zoom(+1));

        addButton(ZoomInButton);
        addButton(ZoomOutButton);

        IPosBaseTileEntity tile = getTileEntity();
        BlockPos basePos = tile.getBasePos();

        assert minecraft != null;
        for (Direction.Axis axis : Direction.Axis.values()){

            int chunkPos = pos.get(axis) >> 4;
            int lowLimit = (axis == Direction.Axis.Y) ? 0 : (chunkPos - 12) << 4;
            int highLimit = (axis == Direction.Axis.Y) ? 255 : ((chunkPos + 12 + 1) << 4) - 1;
            Consumer<String> responder = s -> {
                tile.setBasePos(axis,Integer.parseInt(s));
                Networking.INSTANCE.sendToServer(new ChangePosPacket(Integer.parseInt(s),pos,axis));
            };
            posTextFields[axis.ordinal()] = new NumberTextField(minecraft.font,basePos.get(axis),relative,16+axis.ordinal()*44,151,lowLimit,highLimit,responder);
            addButton(posTextFields[axis.ordinal()]);
        }

        scaleTextFields[0] = new NumberTextField(minecraft.font,16,relative,endInternalGuiX + 5,69,1,50, s -> {});
        scaleTextFields[1] = new NumberTextField(minecraft.font,11,relative,endInternalGuiX + 5,91,1,50, s -> decimalPartScaleY = 0.0F);
        addButton(scaleTextFields[0]);
        addButton(scaleTextFields[1]);


        //Definition of all button that shift the origin position

        int buttonLength = 16; //lengths of buttons
        int buttonHeight = 13;
        int diffText = 13; //shift in Y when mouse is hovering the button
        int yText = 0; //position y in the texture

        //position x in main gui
        //position y in main gui
        //position x in texture
        ImageButton northButton = new ImageButton(
                relative.x + endInternalGuiX + 8, //position x in main gui
                relative.y + 127, //position y in main gui
                buttonLength,
                buttonHeight,
                0, //position x in texture
                yText,
                diffText,
                POS_BUTTON,
                buttons -> changePos(Direction.NORTH));
        ImageButton southButton = new ImageButton(
                relative.x + endInternalGuiX + 8,
                relative.y + 159,
                buttonLength,
                buttonHeight,
                16,
                yText,
                diffText,
                POS_BUTTON,
                buttons -> changePos(Direction.SOUTH));
        ImageButton eastButton = new ImageButton(
                relative.x + endInternalGuiX + 33,
                relative.y + 143,
                buttonLength,
                buttonHeight,
                32,
                yText,
                diffText,
                POS_BUTTON,
                buttons -> changePos(Direction.EAST));
        ImageButton westButton = new ImageButton(
                relative.x + 143,
                relative.y + 143,
                buttonLength,
                buttonHeight,
                48,
                yText,
                diffText,
                POS_BUTTON,
                buttons -> changePos(Direction.WEST));
        ImageButton upButton = new ImageButton(
                relative.x + endInternalGuiX - 1,
                relative.y + 143,
                buttonLength,
                buttonHeight,
                64,
                yText,
                diffText,
                POS_BUTTON,
                buttons -> changePos(Direction.UP));
        ImageButton downButton = new ImageButton(
                relative.x + endInternalGuiX + 16,
                relative.y + 143,
                buttonLength,
                buttonHeight,
                80,
                yText,
                diffText,
                POS_BUTTON,
                buttons -> changePos(Direction.DOWN));
        addButton(northButton);
        addButton(southButton);
        addButton(westButton);
        addButton(eastButton);
        addButton(upButton);
        addButton(downButton);

    }

    //function to change the position using direction buttons

    private void changePos(Direction dir) {
        System.out.println("button with direction "+dir+" clicked with success");
        changePos(dir.getAxis(),dir.getAxisDirection().getStep());
    }

    //function to change the position using offset on axis

    private void changePos(Direction.Axis axis,int offset){
        int index = axis.ordinal();
        int oldPos = Integer.parseInt(posTextFields[index].getValue());
        changePos(oldPos + offset, axis);
    }

    //function to change the position directly by replacing the old value with newPos argument

    private void changePos(int newPos, Direction.Axis axis){
        int index = axis.ordinal();
        posTextFields[index].setValue(String.valueOf(newPos));
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    public void Zoom(int sign){
        scaleTextFields[0].add(sign);
        decimalPartScaleY = decimalPartScaleY + sign * (float)(internalGuiHeight) / internalGuiWidth;
        if (decimalPartScaleY * sign > 1){
            float savedDecimalPartScaleY = decimalPartScaleY - sign;
            scaleTextFields[1].add(sign);
            decimalPartScaleY = savedDecimalPartScaleY;
        }
        System.out.println("Zoom "+(sign==-1 ? "-":"+")+" active");
    }

    private int getScale(Direction.Axis axis){
        if (axis == Direction.Axis.Z)return -1;
        return Integer.parseInt(scaleTextFields[axis.ordinal()].getValue());
    }


    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        Vector2i relative = getRelative();
        GlStateManager._color4f(1.0F, 1.0F, 1.0F, 1.0F);
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bind(GUI);
        this.blit(stack,relative.x, relative.y, 0, 0, WIDTH, HEIGHT);
        drawString(stack, " X ",endInternalGuiX + 19,83,Color.WHITE);
        //display of origin block position
        drawString(stack,"RailSystem",10,10,Color.WHITE);
        drawString(stack,"O",2,20,Color.GREEN);
        drawString(stack,"Position of Block O",12,142,Color.GREEN);
        super.render(stack,mouseX, mouseY, partialTicks);
        ZoomOutButton.active = (isZoomAuthorised(+1));
        ZoomInButton.active = (isZoomAuthorised(-1));
        //function that display all icons on screen
        displayIcons(stack,relative);
    }

    protected void drawString(MatrixStack stack, String content, int offsetX, int offsetY, Color color){
        Vector2i relative = getRelative();
        assert minecraft != null;
        drawString(stack,minecraft.font,content,relative.x+offsetX,relative.y+offsetY,color.getRGB());
    }

    private void displayIcons(MatrixStack stack,Vector2i relative){
        Map<BlockPos, RailType> blockToDisplay = getBlockOnBoard(true);
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bind(ICONS);
        blockToDisplay.forEach((pos,type)->{
            Vector2f posOnBoard = getPosOnBoard(pos,relative);
            boolean isDisabled = type.isSwitch() && isDisabled(pos);
            BlockState state = getBlockState(pos);
            type.render(stack,posOnBoard,getDimensionOnBoard(), state,!isDisabled);
        });
    }

    private Map<BlockPos, RailType> getBlockOnBoard(boolean allowNormalRail){
        Map<BlockPos, RailType> map = new HashMap<>();
        BlockPos basePos = getTileEntity().getBasePos();
        int scaleX = getScale(Direction.Axis.X);
        int scaleY = getScale(Direction.Axis.Y);
        for (int i=0;i<scaleX;i++){
            for (int j=0;j<scaleY;j++){
                BlockPos pos = basePos.offset(i,0,j);
                assert this.minecraft != null;
                Block block = Objects.requireNonNull(this.minecraft.level).getBlockState(pos).getBlock();
                RailType type = RailType.getType(block);
                if (type != null){
                    if (type.isSwitch() || allowNormalRail) map.put(pos,type);
                }
            }
        }
        return map;
    }

    //indicates if a block with position pos is to be rendered on screen

    protected boolean isOnBoard(BlockPos pos) {
        BlockPos basePos = getTileEntity().getBasePos();
        return (pos.getY() == basePos.getY()) &&
                (pos.getX() >= basePos.getX() && pos.getX() < basePos.getX()+getScale(Direction.Axis.X)) && // the blockPos corresponds to the point at the top left of the screen
                (pos.getZ() >= basePos.getZ() && pos.getZ() < basePos.getZ()+getScale(Direction.Axis.Y));
    }

    protected abstract IPosBaseTileEntity getTileEntity();

    protected Vector2f getPosOnBoard(BlockPos pos, Vector2i relative){
        if (!isOnBoard(pos)){
            return Vector2f.ZERO;
        }
        return Util.add(
                relative.toFloatVector(),
                internalGuiDimension,
                Util.directMult(
                        getDimensionOnBoard(),
                        Util.subtract(
                                Util.makeVector(pos),
                                Util.makeVector(getTileEntity().getBasePos())
                        )
                )
        );
    }

    private Vector2f getDimensionOnBoard(){
        return new Vector2f(
                internalGuiWidth /(getScale(Direction.Axis.X)*1.0F),
                internalGuiHeight/(getScale(Direction.Axis.Y)*1.0F));
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //activation of all buttons
        boolean mouseClicked = super.mouseClicked(mouseX,mouseY,button);
        Vector2i relative = getRelative();
        RailData data = getSwitchClicked(mouseX,mouseY,relative);
        if (data != null ) {
            boolean actionDone = onSwitchClicked(data,mouseX,mouseY,relative,button);
            if (actionDone)Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return mouseClicked;
    }

    @Override
    public boolean charTyped(char keyChar, int p_231042_2_) {
        if (keyChar == 'u'){
            changePos(Direction.UP);
            return true;
        }else if (keyChar == 'd') {
            changePos(Direction.DOWN);
            return true;
        }else if (keyChar == '+' || keyChar == '-'){
            // character + is coded with 43 and - with 45
            // when typing + we zoom in,  so we have a zoom of sign -1
            // when typing - we zoom out, so we have a zoom of sign +1
            // a shift of 44 gives the sign conveniently
            int sign = keyChar-44;
            if (isZoomAuthorised(sign)) {
                Zoom(sign);
                return true;
            }
        }
        return super.charTyped(keyChar, p_231042_2_);
    }

    @Override
    public boolean keyPressed(int keyChar, int keyPlacement, int altCtrlMaj) {
        boolean keyPressed = super.keyPressed(keyChar, keyPlacement, altCtrlMaj);
        if (!keyPressed){
            System.out.println(keyChar);
            if (keyChar>261 && keyChar<266){
                // the different arrow are coded with this character :
                // ↑ : 265 -> 2 -> NORTH,
                // ↓ : 264 -> 3 -> SOUTH,
                // ← : 263 -> 4 -> WEST,
                // → : 262 -> 5 -> EAST
                changePos(Direction.from3DDataValue(267-keyChar));
                return true;
            }
        }
        return keyPressed;
    }

    public abstract boolean onSwitchClicked(RailData data, double mouseX, double mouseY, Vector2i relative, int button);

    public abstract boolean isDisabled(BlockPos pos);

    public BlockState getBlockState(BlockPos pos){
        assert this.minecraft != null;
        return Objects.requireNonNull(this.minecraft.level).getBlockState(pos);
    }

    protected RailData getSwitchClicked(double mouseX, double mouseY, Vector2i relative) {
        Map<BlockPos,RailType> switchOnBoard = getBlockOnBoard(false);
        for (BlockPos pos : switchOnBoard.keySet()){
            Vector2f posOnBoard = getPosOnBoard(pos,relative);
            if (Util.isIn(posOnBoard,getDimensionOnBoard(),mouseX,mouseY)){
                return new RailData(switchOnBoard.get(pos),pos);
            }
        }
        return null;
    }

    // isRightUpNearestOnScreen function return a boolean that specified if the mouse is nearer from the right up switch part on the screen than the left down switch for tjd switch
    // if axis of the switch is Z (dir = North), the ld part is in left down, so we use the diagonal from left up to right down as border
    // its equation is Y = iconHeight/iconLength * X (origin is in left up)
    // ld is nearer when mouseY > Y (y is from up to down) => mouseY > iconHeight/iconLength * mouseX => iconLength *mouseY > iconHeight * mouseX
    // if axis of the switch is X (dir = East), the ld part is in right up, so we use the diagonal from left up to right down as border
    // its equation is Y = iconHeight - iconHeight/iconLength * X (origin is in left up)
    // ld is nearer when mouseY < Y (y is from up to down) => mouseY < iconHeight - iconHeight/iconLength * mouseX => mouseY * iconLength < iconArea - iconHeight * mouseX
    // IMPORTANT : all the function is inverted -> we calculate the boolean that indicates if left down is nearest and we return the opposite boolean

    protected boolean isRightUpNearestOnScreen(BlockPos pos, Vector2i relative, double mouseX, double mouseY){
        Vector2f posOnBoard = getPosOnBoard(pos,relative);
        float iconLength = internalGuiWidth / getScale(Direction.Axis.X);
        float iconHeight = internalGuiHeight / getScale(Direction.Axis.Y);
        double relativeMouseX = mouseX-posOnBoard.x;
        double relativeMouseY = mouseY-posOnBoard.y;
        assert this.minecraft != null;
        World world = this.minecraft.level;
        assert world != null;
        BlockState state = world.getBlockState(pos);
        Direction axis_direction = state.getValue(SwitchDoubleSlip.FACING_AXE);
        return (axis_direction == Direction.NORTH) ? !(relativeMouseY * iconLength > iconHeight * relativeMouseX) : !(relativeMouseY * iconLength < iconHeight * (iconLength - relativeMouseX));
    }

}

