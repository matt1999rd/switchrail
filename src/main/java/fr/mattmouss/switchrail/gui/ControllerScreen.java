package fr.mattmouss.switchrail.gui;


import com.mojang.blaze3d.platform.GlStateManager;
import fr.mattmouss.switchrail.SwitchRailMod;
import fr.mattmouss.switchrail.blocks.ControllerTile;
import fr.mattmouss.switchrail.enum_rail.SwitchType;
import fr.mattmouss.switchrail.enum_rail.Tjd_Position;
import fr.mattmouss.switchrail.switchblock.Switch;
import fr.mattmouss.switchrail.switchblock.Switch_Tjd;
import fr.mattmouss.switchrail.switchdata.SwitchData;

import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.state.properties.BlockStateProperties;

import net.minecraft.state.properties.RailShape;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ControllerScreen extends ContainerScreen<ControllerContainer> implements IGuiEventListener {


    private ResourceLocation GUI = new ResourceLocation(SwitchRailMod.MODID, "textures/gui/controller_gui.png");
    private ResourceLocation POS_BUTTON = new ResourceLocation(SwitchRailMod.MODID,"textures/gui/posbutton.png");
    private ResourceLocation OTHERS_PICTURE = new ResourceLocation(SwitchRailMod.MODID,"textures/gui/rail_picture.png");

    private static final int WIDTH = 212;
    private static final int HEIGHT = 174;


    private int scaleX = 16;
    private int scaleY = 11;

    private int tailleX = 144 / scaleX;
    private int tailleY = 99 / scaleY; // attention à la valeur de la taille de l'ecran en y

    private int x_origine = 13;
    private int y_origine = 22;

    private boolean addone = false;
    private boolean delone = true;

    private Button ZoomInButton;

    private Button ZoomOutButton;

    private Button DoneButton;

    private ImageButton NorthButton;
    private ImageButton SouthButton;
    private ImageButton EastButton;
    private ImageButton WestButton;
    private ImageButton UpButton;
    private ImageButton DownButton;


    private static final int white = 0xffffff;
    private static final int green = 0x00ff00;

    private ControllerTile te;


    public ControllerScreen(ControllerContainer container, PlayerInventory inventory, ITextComponent title) {
        super(container,inventory,title);
        te = container.getTileEntity();
    }

    public void init() {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;

        DoneButton =new Button(relX + 12,
                relY + 122,
                146,
                20,
                "Done",
                button -> onClose());

        addButton(DoneButton);

        ZoomInButton = new Button(relX + 160,
                relY + 26,
                39,
                20,
                "Zoom+",
                button -> ZoomIn());
        ZoomOutButton = new Button(relX + 160,
                relY + 47,
                39,
                20,
                "Zoom-",
                button -> ZoomOut());



        addButton(ZoomInButton);
        addButton(ZoomOutButton);

        /**
         * Definition de tous les boutons de déplacement de la position d'origine
         */
        int tailleX_button = 16; //taille des boutons
        int tailleY_button = 13;
        int diffText = 13; //décalage en y lorsque la souris se pose dessus
        int yText = 0; //position y dans l'image

        NorthButton = new ImageButton(
                relX+169, //position x dans la gui principal
                relY+126, //position y dans la gui principal
                tailleX_button,
                tailleY_button,
                0, //position x dans l'image
                yText,
                diffText,
                POS_BUTTON,
                buttons->changePos(Direction.NORTH));
        SouthButton = new ImageButton(
                relX+169,
                relY+159,
                tailleX_button,
                tailleY_button,
                16,
                yText,
                diffText,
                POS_BUTTON,
                buttons-> changePos(Direction.SOUTH));
        EastButton = new ImageButton(
                relX+194,
                relY+142,
                tailleX_button,
                tailleY_button,
                32,
                yText,
                diffText,
                POS_BUTTON,
                buttons-> changePos(Direction.EAST));
        WestButton = new ImageButton(
                relX+143,
                relY+142,
                tailleX_button,
                tailleY_button,
                48,
                yText,
                diffText,
                POS_BUTTON,
                buttons-> changePos(Direction.WEST));
        UpButton = new ImageButton(
                relX+160,
                relY+142,
                tailleX_button,
                tailleY_button,
                64,
                yText,
                diffText,
                POS_BUTTON,
                buttons-> changePos(Direction.UP));
        DownButton = new ImageButton(
                relX+177,
                relY+142,
                tailleX_button,
                tailleY_button,
                80,
                yText,
                diffText,
                POS_BUTTON,
                buttons-> changePos(Direction.DOWN));
        addButton(NorthButton);
        addButton(SouthButton);
        addButton(WestButton);
        addButton(EastButton);
        addButton(UpButton);
        addButton(DownButton);

    }

    //pour changer la position avec les boutons

    private void changePos(Direction dir) {
        System.out.println("Bouton "+dir+" Button appuyé avec succès");
        te.pos_base = te.pos_base.offset(dir);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    public void changeSwitch(SwitchData data, double mouseX, double mouseY, int relX, int relY) {
        if (data.type.getMeta() == 1) {
            int x = getXOnBoard(data.pos,relX);
            int y = getYOnBoard(data.pos,relY);
            double mouseX_rel = mouseX-x;
            double mouseY_rel = mouseY-y;
            World world = te.getWorld();
            BlockState state = world.getBlockState(data.pos);
            Tjd_Position tjd_position = state.get(Switch_Tjd.SWITCH_POSITION);
            Direction dir = state.get(Switch_Tjd.FACING_AXE);
            Switch_Tjd switch_tjd = (Switch_Tjd)(state.getBlock());
            System.out.println("changing switch using gui successfully done !");
            switch (dir) {
                case NORTH:
                    //ld est plus proche si on est dans la partie inferieur à la diagonale haut gauche - bas droite
                    switch_tjd.updatePowerState(world,state,data.pos,7,mouseX_rel<mouseY_rel,tjd_position,Direction.NORTH);
                    break;
                case EAST:
                    //ld est plus proche si on est dans la partie inferieur à la diagonale haut droite - bas gauche
                    switch_tjd.updatePowerState(world,state,data.pos,7,(mouseX_rel>tailleX/2 && mouseY_rel>tailleY/2),tjd_position,Direction.EAST);
                    break;
                case WEST:
                case SOUTH:
                case DOWN:
                case UP:
                    throw new IllegalArgumentException("No such direction available for this block :"+dir);
            }
        } else {


            World world= te.getWorld();
            BlockState state = world.getBlockState(data.pos);
            Block block = state.getBlock();
            if (block instanceof Switch) {
                Switch sw = (Switch) block;
                System.out.println("changing switch using gui successfully done !");
                sw.updatePoweredState(world, state, data.pos, null, 7,true);
            }
        }
    }

    public void ZoomIn() {
        System.out.println("Zoom + active");
        scaleX = (addone) ? scaleX - 1 : scaleX - 2;
        addone = !addone;
        delone = !delone;
        scaleY -= 1;
        tailleX = 144 / scaleX;
        tailleY = 99 / scaleY;
    }

    public void ZoomOut() {
        System.out.println("Zoom - active");
        scaleX = (delone) ? scaleX + 1 : scaleX + 2;
        delone = !delone;
        addone = !addone;
        scaleY += 1;
        tailleX = 144 / scaleX;
        tailleY = 99 / scaleY;
    }


    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI);
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;
        this.blit(relX, relY, 0, 0, WIDTH, HEIGHT);
        this.drawString(Minecraft.getInstance().fontRenderer, "RailSystem", relX + 10, relY + 10, white);
        this.drawString(Minecraft.getInstance().fontRenderer, scaleX + " X " + scaleY, relX + 160, relY + 68, white);
        ZoomOutButton.active = (scaleX != 16);
        ZoomInButton.active = (scaleX != 4);
        //affichage des switchs enregistrés
        displaySwitch(relX,relY);
        //affichage des rails normaux automatiquement
        displayNormalRail(relX,relY);
        //affichage du block de controle
        displayControllerBlock(relX,relY);
        //affichage de la position du block d'origine
        this.drawString(minecraft.fontRenderer,"O",relX+2,relY+20,green);
        this.drawString(minecraft.fontRenderer,"Position du Block O",relX+12,relY+142,green);
        this.drawString(minecraft.fontRenderer,String.valueOf(te.pos_base.getX()),relX+18,relY+153,white);
        this.drawString(minecraft.fontRenderer,String.valueOf(te.pos_base.getY()),relX+62,relY+153,white);
        this.drawString(minecraft.fontRenderer,String.valueOf(te.pos_base.getZ()),relX+106,relY+153,white);
        //affichage des boutons de controle de la position
        GlStateManager.enableBlend();
        super.render(mouseX, mouseY, partialTicks);


    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

    }

    private void displayControllerBlock(int relX, int relY) {
        this.minecraft.getTextureManager().bindTexture(OTHERS_PICTURE);
        int[] scaleYList = {0,9,18,29,41,55,71,114,171}; //cumul des scaleY
        if (isOnBoard(te.getPos())){
            int i= 11-scaleY;
            int x_debut_affichage =getXOnBoard(te.getPos(),relX);
            int y_debut_affichage =getYOnBoard(te.getPos(),relY);
            int x_debut_image = (i == 7 || i==8) ? 5*tailleX : 11*tailleX ;
            int y_debut_image =scaleYList[i];
            //System.out.println("debut de l'image en ("+x_debut_image+" ; "+y_debut_image+" )");
            //données de l'image du controller stockées dans la texture rail_picture.png
            this.blit(x_debut_affichage,y_debut_affichage,x_debut_image,y_debut_image,tailleX,tailleY);
        }
    }


    private void displayNormalRail(int relX, int relY) {
        List<BlockPos> normalRails = getNormalRailOnBoard();
        for (BlockPos pos: normalRails){
            String type = te.getWorld().getBlockState(pos).getBlock().getClass().getName();
            Boolean isCrossed = !type.startsWith("net"); // les blocks que je n'ai pas créé
            showSpecifiedNormalRail(pos,isCrossed,relX,relY);
        }

    }


    private void showSpecifiedNormalRail(BlockPos pos, Boolean isCrossed, int relativeX, int relativeY) {
        this.minecraft.getTextureManager().bindTexture(OTHERS_PICTURE);
        int[] scaleYList = {0,9,18,29,41,55,71,90,114,138,171}; //cumul des scaleY
        int i = 11-scaleY;
        int x_debut_affichage =getXOnBoard(pos,relativeX);
        int y_debut_affichage =getYOnBoard(pos,relativeY);
        int order;
        int x_debut_image;
        int y_debut_image;
        if (isCrossed){
            order = 10;
        }else {
            BlockState state = te.getWorld().getBlockState(pos);
            if (state.has(BlockStateProperties.RAIL_SHAPE)) {
                order = getMeta(state.get(BlockStateProperties.RAIL_SHAPE));
            } else if (state.has(BlockStateProperties.RAIL_SHAPE_STRAIGHT)){
                order = getMeta(state.get(BlockStateProperties.RAIL_SHAPE_STRAIGHT));
            } else {
                throw new IllegalArgumentException("No rail block are selected here : "+state.getBlock());
            }
        }
        if (i<7){
            x_debut_image = order*tailleX;
            y_debut_image= scaleYList[i];
        }else if (i==7){
            x_debut_image = (order%6)*tailleX;
            int ind = MathHelper.intFloorDiv(order,6)+i;
            y_debut_image= scaleYList[ind];
        }else if (i==8){
            int ind = MathHelper.intFloorDiv(order,6)+i;
            x_debut_image = (order%6)*tailleX;
            y_debut_image= scaleYList[ind+1];
        }else {
            throw new IllegalArgumentException("No such scale is authorized : "+scaleX);
        }
        //System.out.println("coordonée x de debut d'affichage : "+x_debut_affichage);
        //System.out.println("coordonée y de debut d'affichage : "+y_debut_affichage);
        //System.out.println("coordonée x de debut d'image : "+x_debut_image);
        //System.out.println("coordonée y de debut d'image : "+y_debut_image);
        //System.out.println("taille de l'image : "+tailleX+" x "+tailleY+" .");
        this.blit(x_debut_affichage,y_debut_affichage,x_debut_image,y_debut_image,tailleX,tailleY);

    }

    private int getMeta(RailShape shape){
        switch (shape){
            case NORTH_SOUTH:
                return 0;
            case EAST_WEST:
                return 1;
            case ASCENDING_EAST:
                return 2;
            case ASCENDING_WEST:
                return 3;
            case ASCENDING_NORTH:
                return 4;
            case ASCENDING_SOUTH:
                return 5;
            case SOUTH_EAST:
                return 6;
            case SOUTH_WEST:
                return 7;
            case NORTH_WEST:
                return 8;
            case NORTH_EAST:
                return 9;
            default:
                return -1;
        }
    }


    private List<BlockPos> getNormalRailOnBoard() {
        List<BlockPos> list = new ArrayList<>();
        for (int i=0;i<scaleX;i++){
            for (int j=0;j<scaleY;j++){
                BlockPos pos =te.pos_base.add(i,0,j);
                Block block = te.getWorld().getBlockState(pos).getBlock();
                if (block instanceof AbstractRailBlock && !(block instanceof Switch)){
                    list.add(pos);
                }
            }
        }
        return list;
    }

    public List<SwitchData> getSwitchOnBoard(){
        List<SwitchData> list = new ArrayList<>();
        for (int i=0;i<scaleX;i++){
            for (int j=0;j<scaleY;j++){
                BlockPos pos =te.pos_base.add(i,0,j);
                Block block = te.getWorld().getBlockState(pos).getBlock();

                if (block instanceof Switch){
                    SwitchType type = ((Switch)block).getType();
                    list.add(new SwitchData(type,pos));
                }
            }
        }
        return list;
    }


    private void displaySwitch(int RelativeX, int RelativeY) {
        //si on veut les afficher directement
        List<SwitchData> dataList = getSwitchOnBoard();
        //si on utilise le block de Register
        //List<SwitchData> dataList = te.getSwitch();
        for (SwitchData data : dataList) {
            showSpecifiedSwitch(data,RelativeX,RelativeY);
        }
    }

    private void showSpecifiedSwitch(SwitchData data, int relativeX, int relativeY) {
        ResourceLocation location = data.getResourceLocation(te);
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bindTexture(location);
        if (this.isOnBoard(data.pos)){
            int x_debut_affichage =getXOnBoard(data.pos,relativeX);
            int y_debut_affichage =getYOnBoard(data.pos,relativeY);
            int x_debut_image =data.getXDebutImg(tailleX,te,scaleY);
            int y_debut_image =data.getYDebutImg(scaleY,te);
            //System.out.println("coordonée x de debut d'affichage : "+x_debut_affichage);
            //System.out.println("coordonée y de debut d'affichage : "+y_debut_affichage);
            //System.out.println("coordonée x de debut d'image : "+x_debut_image);
            //System.out.println("coordonée y de debut d'image : "+y_debut_image);
            //System.out.println("taille de l'image : "+tailleX+" x "+tailleY+" .");
            this.blit(x_debut_affichage,y_debut_affichage,x_debut_image,y_debut_image,tailleX,tailleY);
        }

    }

    //permet de savoir si un switch stocké dans un switchdata est bien affichable sur l'écran (utilise uniquement les blockpos

    private boolean isOnBoard(BlockPos pos) {
        return (pos.getY() == te.pos_base.getY()) &&
                (pos.getX() >= te.pos_base.getX() && pos.getX() < te.pos_base.getX()+scaleX) && // le blockPos corrrespond au point en haut à gauche de la fenetre
                (pos.getZ() >= te.pos_base.getZ() && pos.getZ() < te.pos_base.getZ()+scaleY);
    }

    private int getXOnBoard(BlockPos pos,int RelativeX){
        if (!isOnBoard(pos)){
            return -1;
        }
        int i =pos.getX()-te.pos_base.getX();
        return x_origine+RelativeX+i*tailleX;
    }

    private int getYOnBoard(BlockPos pos,int RelativeY){
        if (!isOnBoard(pos)){
            return -1;
        }
        int j= pos.getZ()-te.pos_base.getZ();
        return y_origine+RelativeY+j*tailleY;
    }




    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //activation de tous les boutons
        ZoomInButton.mouseClicked(mouseX,mouseY,button);
        ZoomOutButton.mouseClicked(mouseX,mouseY,button);
        DoneButton.mouseClicked(mouseX,mouseY,button);
        NorthButton.mouseClicked(mouseX,mouseY,button);
        SouthButton.mouseClicked(mouseX,mouseY,button);
        EastButton.mouseClicked(mouseX,mouseY,button);
        WestButton.mouseClicked(mouseX,mouseY,button);
        UpButton.mouseClicked(mouseX,mouseY,button);
        DownButton.mouseClicked(mouseX,mouseY,button);

        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;

        if (getSwitchClicked(mouseX,mouseY,relX,relY) != null) {
            Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            SwitchData data = getSwitchClicked(mouseX,mouseY,relX,relY);
            changeSwitch(data,mouseX,mouseY,relX,relY);
            return true;
        }
        return false;
    }


    private SwitchData getSwitchClicked(double mouseX,double mouseY,int RelativeX,int RelativeY) {
        for (SwitchData data : getSwitchOnBoard() /*te.getSwitch()*/){
            int x = getXOnBoard(data.pos,RelativeX);
            int y = getYOnBoard(data.pos,RelativeY);
            if ((mouseX>x && mouseX<x+tailleX) && (mouseY>y && mouseY<y+tailleY)){
                return data;
            }
        }
        return null;
    }

}

