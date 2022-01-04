package fr.mattmouss.switchrail.gui;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import fr.mattmouss.switchrail.SwitchRailMod;
import fr.mattmouss.switchrail.blocks.IPosBaseTileEntity;
import fr.mattmouss.switchrail.enum_rail.RailType;
import fr.mattmouss.switchrail.network.ChangePosPacket;
import fr.mattmouss.switchrail.network.Networking;
import fr.mattmouss.switchrail.other.Util;
import fr.mattmouss.switchrail.switchblock.Switch_Tjd;
import fr.mattmouss.switchrail.switchdata.RailData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class RailScreen extends Screen implements IGuiEventListener {

    private final ResourceLocation GUI = new ResourceLocation(SwitchRailMod.MOD_ID, "textures/gui/controller_gui.png");
    private final ResourceLocation POS_BUTTON = new ResourceLocation(SwitchRailMod.MOD_ID,"textures/gui/posbutton.png");
    private static final ResourceLocation ICONS = new ResourceLocation(SwitchRailMod.MOD_ID,"textures/gui/controller_icons.png");

    private static final int WIDTH = 212;
    private static final int HEIGHT = 174;
    private static final Vector2f internalGuiDimension = new Vector2f(13,22);
    private static final int internalGuiWidth = 144;
    private static final int internalGuiHeight = 99;

    private int scaleX = 16;
    private int scaleY = 11;

    protected int iconLength = 144 / scaleX;
    protected int iconHeight = 99 / scaleY; // beware of height value in screen



    private boolean delone = false;

    private Button ZoomInButton;

    private Button ZoomOutButton;

    protected final BlockPos pos;


    private static final int white = 0xffffff;
    private static final int green = 0x00ff00;



    public RailScreen(BlockPos pos) {
        super(ITextComponent.nullToEmpty("RailSystem"));
        this.pos = pos;
    }


    public void init() {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;

        Button doneButton = new Button(relX + 12,
                relY + 122,
                146,
                20,
                ITextComponent.nullToEmpty("Done"),
                button -> onClose());

        addButton(doneButton);

        ZoomInButton = new Button(relX + 160,
                relY + 26,
                39,
                20,
                ITextComponent.nullToEmpty("Zoom+"),
                button -> ZoomIn());
        ZoomOutButton = new Button(relX + 160,
                relY + 47,
                39,
                20,
                ITextComponent.nullToEmpty("Zoom-"),
                button -> ZoomOut());



        addButton(ZoomInButton);
        addButton(ZoomOutButton);

        //Definition of all button that shift the origin position

        int buttonLength = 16; //lengths of buttons
        int buttonHeight = 13;
        int diffText = 13; //shift in Y when mouse is hovering the button
        int yText = 0; //position y in the texture

        //position x in main gui
        //position y in main gui
        //position x in texture
        ImageButton northButton = new ImageButton(
                relX + 169, //position x in main gui
                relY + 126, //position y in main gui
                buttonLength,
                buttonHeight,
                0, //position x in texture
                yText,
                diffText,
                POS_BUTTON,
                buttons -> changePos(Direction.NORTH));
        ImageButton southButton = new ImageButton(
                relX + 169,
                relY + 159,
                buttonLength,
                buttonHeight,
                16,
                yText,
                diffText,
                POS_BUTTON,
                buttons -> changePos(Direction.SOUTH));
        ImageButton eastButton = new ImageButton(
                relX + 194,
                relY + 142,
                buttonLength,
                buttonHeight,
                32,
                yText,
                diffText,
                POS_BUTTON,
                buttons -> changePos(Direction.EAST));
        ImageButton westButton = new ImageButton(
                relX + 143,
                relY + 142,
                buttonLength,
                buttonHeight,
                48,
                yText,
                diffText,
                POS_BUTTON,
                buttons -> changePos(Direction.WEST));
        ImageButton upButton = new ImageButton(
                relX + 160,
                relY + 142,
                buttonLength,
                buttonHeight,
                64,
                yText,
                diffText,
                POS_BUTTON,
                buttons -> changePos(Direction.UP));
        ImageButton downButton = new ImageButton(
                relX + 177,
                relY + 142,
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

    //function to change the position using buttons

    private void changePos(Direction dir) {
        System.out.println("button with direction "+dir+" clicked with success");
        IPosBaseTileEntity tile = getTileEntity();
        tile.changePosBase(dir);
        Networking.INSTANCE.sendToServer(new ChangePosPacket(dir,pos));
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    public void ZoomIn() {
        System.out.println("Zoom + active");
        scaleX = (delone) ? scaleX - 1 : scaleX - 2;
        delone = !delone;
        scaleY -= 1;
        iconLength = 144 / scaleX;
        iconHeight = 99 / scaleY;
    }

    public void ZoomOut() {
        System.out.println("Zoom - active");
        scaleX = (!delone) ? scaleX + 1 : scaleX + 2;
        delone = !delone;
        scaleY += 1;
        iconLength = 144 / scaleX;
        iconHeight = 99 / scaleY;
    }


    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;
        GlStateManager._color4f(1.0F, 1.0F, 1.0F, 1.0F);
        IPosBaseTileEntity tile = getTileEntity();
        BlockPos basePos = tile.getPosBase();
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bind(GUI);
        this.blit(stack,relX, relY, 0, 0, WIDTH, HEIGHT);
        drawString(stack,Minecraft.getInstance().font, scaleX + " X " + scaleY, relX + 160, relY + 68, white);
        //display of origin block position
        assert minecraft != null;
        drawString(stack,minecraft.font, "RailSystem", relX + 10, relY + 10, white);
        drawString(stack,minecraft.font,"O",relX+2,relY+20,green);
        drawString(stack,minecraft.font,"Position of Block O",relX+12,relY+142,green);
        drawString(stack,minecraft.font,String.valueOf(basePos.getX()),relX+18,relY+153,white);
        drawString(stack,minecraft.font,String.valueOf(basePos.getY()),relX+62,relY+153,white);
        drawString(stack,minecraft.font,String.valueOf(basePos.getZ()),relX+106,relY+153,white);
        super.render(stack,mouseX, mouseY, partialTicks);
        ZoomOutButton.active = (scaleX != 16);
        ZoomInButton.active = (scaleX != 4);
        //function that display all icons on screen
        displayIcons(stack,relX,relY);
    }

    private void displayIcons(MatrixStack stack,int relX, int relY){
        Map<BlockPos, RailType> blockToDisplay = getBlockOnBoard(true);
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bind(ICONS);
        blockToDisplay.forEach((pos,type)->{
            Vector2f posOnBoard = getPosOnBoard(pos,new Vector2f(relX,relY));
            boolean isDisabled = type.isSwitch() && isDisabled(pos);
            BlockState state = getBlockState(pos);
            type.render(stack,posOnBoard,getDimensionOnBoard(), state,!isDisabled);
        });
    }

    private Map<BlockPos, RailType> getBlockOnBoard(boolean allowNormalRail){
        Map<BlockPos, RailType> map = new HashMap<>();
        BlockPos basePos = getTileEntity().getPosBase();
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
        BlockPos basePos = getTileEntity().getPosBase();
        return (pos.getY() == basePos.getY()) &&
                (pos.getX() >= basePos.getX() && pos.getX() < basePos.getX()+scaleX) && // the blockPos corresponds to the point at the top left of the screen
                (pos.getZ() >= basePos.getZ() && pos.getZ() < basePos.getZ()+scaleY);
    }

    protected abstract IPosBaseTileEntity getTileEntity();

    protected Vector2f getPosOnBoard(BlockPos pos, Vector2f relative){
        if (!isOnBoard(pos)){
            return Vector2f.ZERO;
        }
        return Util.add(
                relative,
                internalGuiDimension,
                Util.directMult(
                        getDimensionOnBoard(),
                        Util.subtract(
                                Util.makeVector(pos),
                                Util.makeVector(getTileEntity().getPosBase())
                        )
                )
        );
    }

    private Vector2f getDimensionOnBoard(){
        return new Vector2f(internalGuiWidth/(scaleX*1.0F),internalGuiHeight/(scaleY*1.0F));
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //activation of all buttons
        boolean mouseClicked = super.mouseClicked(mouseX,mouseY,button);
        Vector2f relative = new Vector2f(
                (float) (this.width - WIDTH) / 2,
                (float) (this.height - HEIGHT) / 2);

        RailData data = getSwitchClicked(mouseX,mouseY,relative);
        if (data != null ) {
            boolean actionDone = onSwitchClicked(data,mouseX,mouseY,relative,button);
            if (actionDone)Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return mouseClicked;
    }

    public abstract boolean onSwitchClicked(RailData data,double mouseX,double mouseY,Vector2f relative,int button);

    public abstract boolean isDisabled(BlockPos pos);

    public BlockState getBlockState(BlockPos pos){
        assert this.minecraft != null;
        return Objects.requireNonNull(this.minecraft.level).getBlockState(pos);
    }

    protected RailData getSwitchClicked(double mouseX, double mouseY, Vector2f relative) {
        Map<BlockPos,RailType> switchOnBoard = getBlockOnBoard(false);
        for (BlockPos pos : switchOnBoard.keySet()){
            Vector2f posOnBoard = getPosOnBoard(pos,relative);
            if (Util.isIn(posOnBoard,getDimensionOnBoard(),mouseX,mouseY)){
                return new RailData(switchOnBoard.get(pos),pos);
            }
        }
        return null;
    }

    // isLeftDownNearestOnScreen function return a boolean that specified if the mouse is nearer from the left down switch part on the screen than the right up switch for tjd switch
    // if axis of the switch is Z (dir = North), the ld part is in left down, so we use the diagonal from left up to right down as border
    // its equation is Y = iconHeight/iconLength * X (origin is in left up)
    // ld is nearer when mouseY > Y (y is from up to down) => mouseY > iconHeight/iconLength * mouseX => iconLength *mouseY > iconHeight * mouseX
    // if axis of the switch is X (dir = East), the ld part is in right up, so we use the diagonal from left up to right down as border
    // its equation is Y = iconHeight - iconHeight/iconLength * X (origin is in left up)
    // ld is nearer when mouseY < Y (y is from up to down) => mouseY < iconHeight - iconHeight/iconLength * mouseX => mouseY * iconLength < iconArea - iconHeight * mouseX
    protected boolean isLeftDownNearestOnScreen(BlockPos pos, Vector2f relative, double mouseX, double mouseY){
        Vector2f posOnBoard = getPosOnBoard(pos,relative);
        double relativeMouseX = mouseX-posOnBoard.x;
        double relativeMouseY = mouseY-posOnBoard.y;
        assert this.minecraft != null;
        World world = this.minecraft.level;
        assert world != null;
        BlockState state = world.getBlockState(pos);
        Direction axis_direction = state.getValue(Switch_Tjd.FACING_AXE);
        return (axis_direction == Direction.NORTH)? relativeMouseY * iconLength > iconHeight*relativeMouseX :
                relativeMouseY * iconLength < iconHeight*(iconLength-relativeMouseX) ;
    }

}

