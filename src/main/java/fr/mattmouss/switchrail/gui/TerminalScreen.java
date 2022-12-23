package fr.mattmouss.switchrail.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import fr.mattmouss.switchrail.blocks.IPosBaseTileEntity;
import fr.mattmouss.switchrail.blocks.SwitchTerminal;
import fr.mattmouss.switchrail.blocks.TerminalTile;
import fr.mattmouss.switchrail.enum_rail.Corners;
import fr.mattmouss.switchrail.enum_rail.RailType;
import fr.mattmouss.switchrail.network.Networking;
import fr.mattmouss.switchrail.network.TerminalScreenPacket;
import fr.mattmouss.switchrail.other.Vector2i;
import fr.mattmouss.switchrail.switchblock.Switch;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.gui.GuiUtils;
import com.mojang.datafixers.util.Pair;

import java.awt.*;
import java.util.ArrayList;

public class TerminalScreen extends RailScreen {
    public static final byte ADD_SWITCH_ACTION = 0;
    public static final byte REMOVE_SWITCH_ACTION = 1;
    public static final byte SET_SWITCH_ACTION = 2;
    private static final String conflictError = "Conflict Error : one of the switch is already activated by another terminal";
    private int ErrorState = 0;
    private static final int ERROR_SCREEN_WIDTH = 107;
    private static final int ERROR_SCREEN_BEGINNING_X = 161;
    private static final int ERROR_SCREEN_BEGINNING_Y = 0;

    protected TerminalScreen(BlockPos pos) {
        super(pos);
    }

    @Override
    protected IPosBaseTileEntity getTileEntity() {
        assert this.minecraft != null;
        assert this.minecraft.level != null;
        TileEntity tileEntity = this.minecraft.level.getBlockEntity(pos);
        if (tileEntity instanceof TerminalTile){
            return (TerminalTile) tileEntity;
        }
        assert tileEntity != null;
        throw new IllegalStateException("BlockPos of the terminal screen ("+pos+") is not associated with a correct tile entity (found tile entity of type "+tileEntity.getClass()+" ) !");
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bind(POS_BUTTON);
        Vector2i relative = getRelative();
        this.blit(stack,relative.x+ERROR_SCREEN_BEGINNING_X,relative.y+ERROR_SCREEN_BEGINNING_Y,0,26,124,26);
        TerminalTile tile = (TerminalTile) getTileEntity();
        BlockState state = tile.getBlockState();
        if (state.getValue(BlockStateProperties.POWERED)){
            if (state.getValue(SwitchTerminal.IS_BLOCKED)){
                tile.tryUnblockTerminal();
            }else {
                tile.tryBlockTerminal();
            }
        }
        if (tile.getBlockState().getValue(SwitchTerminal.IS_BLOCKED)){
            renderErrorWithMovement(stack);
            if (isSwitchHoveredDisabled(mouseX, mouseY)){
                drawHoveringText(stack, mouseX, mouseY);
            }
        }else {
            drawString(stack,
                    "No Conflict Error",
                    ERROR_SCREEN_BEGINNING_X+11,ERROR_SCREEN_BEGINNING_Y+9,Color.GREEN);
            ErrorState = 0;
        }
    }

    private void renderErrorWithMovement(MatrixStack stack) {
        String initialDisplay = reduceStringToScreen(conflictError);
        ErrorState++;
        String stringToDisplay;
        int offset = 0;
        if (ErrorState<0){
            stringToDisplay = initialDisplay.substring(0,initialDisplay.length()+ErrorState/6);
            assert minecraft != null;
            offset = ERROR_SCREEN_WIDTH-minecraft.font.width(stringToDisplay);

        }else {
            stringToDisplay = conflictError.substring(ErrorState / 6);
            stringToDisplay = reduceStringToScreen(stringToDisplay);
            if (stringToDisplay.isEmpty()) ErrorState = -6 * initialDisplay.length() - 5;
        }
        drawString(stack,stringToDisplay, ERROR_SCREEN_BEGINNING_X+11+offset,ERROR_SCREEN_BEGINNING_Y+9,Color.RED);
    }

    private String reduceStringToScreen(String originalString){
        int maxString = originalString.length();
        assert minecraft != null;
        while (maxString != 0 && minecraft.font.width(originalString.substring(0,maxString))>ERROR_SCREEN_WIDTH){
            maxString--;
        }
        return originalString.substring(0,maxString);
    }

    private void drawHoveringText(MatrixStack stack,int mouseX,int mouseY){
        ArrayList<ITextProperties> textLines = Lists.newArrayList();
        textLines.add(ITextProperties.of("Conflict Switch", Style.EMPTY.applyFormats(TextFormatting.BOLD, TextFormatting.RED)));
        Vector2i relative = getRelative();
        assert minecraft != null;
        GuiUtils.drawHoveringText(stack,textLines,mouseX,mouseY,WIDTH,HEIGHT,WIDTH+relative.x-mouseX,minecraft.font);
    }

    private boolean isSwitchHoveredDisabled(int mouseX,int mouseY){
        Pair<RailType, BlockPos> data = getSwitchClicked(mouseX,mouseY,getRelative());
        if (data != null && !isDisabled(data.getSecond())){
            assert minecraft != null;
            assert minecraft.level != null;
            BlockState state = minecraft.level.getBlockState(data.getSecond());
            return !state.getValue(BlockStateProperties.ENABLED);
        }
        return false;
    }

    @Override
    public boolean onSwitchClicked(Pair<RailType,BlockPos> data, double mouseX, double mouseY, Vector2i relative, int button) {
        TerminalTile tile = (TerminalTile) getTileEntity();
        TerminalScreenPacket packet = null;
        BlockPos switchBlockPos = data.getSecond();
        // flag is a byte that indicates boolean used for action on set switch part
        // if flag = 0x00 -> set position of normal switch
        // if flag = 0xA1 -> set position of double slip switch
        //      if flag = 0x01 -> set position of dds knowing that right up was clicked
        //      if flag = 0x11 -> set position of dds knowing that left down was clicked
        byte flag = 0;
        if (button == 0){
            //when we click on a switch with the left button of the mouse, we add or modify the switch
            if (isDisabled(switchBlockPos)){
                tile.addSwitch(switchBlockPos);
                packet = new TerminalScreenPacket(flag,pos,switchBlockPos, ADD_SWITCH_ACTION);
            }else {
                BlockState state = tile.getSwitchValue(switchBlockPos);
                if (!(state.getBlock() instanceof Switch)){
                    throw new IllegalStateException("Block switchBlockPos clicked is not pointing toward a switch !");
                }
                Switch switchBlock = (Switch) state.getBlock();
                EnumProperty<Corners> property = switchBlock.getSwitchPositionProperty();
                if (data.getFirst() == RailType.DOUBLE_SLIP){
                    flag += 1;
                    boolean isLeftDownNearest = isRightUpNearestOnScreen(switchBlockPos, relative, mouseX, mouseY);
                    if (isLeftDownNearest)flag += 2;
                    Corners corners = state.getValue(property);
                    tile.setPosition(switchBlockPos,state.setValue(property,corners.moveDssSwitch(isLeftDownNearest)));
                }else {
                    tile.setPosition(switchBlockPos,state.cycle(property));
                }
                packet = new TerminalScreenPacket(flag,pos,switchBlockPos,SET_SWITCH_ACTION);
            }
        }else if (button == 1){
            //when we click on a switch with the right button of the mouse, we remove the switch if it is present
            if (!isDisabled(switchBlockPos)){
                tile.removeSwitch(switchBlockPos);
                packet = new TerminalScreenPacket(flag,pos,switchBlockPos,REMOVE_SWITCH_ACTION);
            }
        }
        if (packet != null)Networking.INSTANCE.sendToServer(packet);
        return true;
    }

    @Override
    public boolean isDisabled(BlockPos pos) {
        TerminalTile tile = (TerminalTile) getTileEntity();
        return !tile.hasSwitch(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        BlockState state = super.getBlockState(pos);
        TerminalTile tile = (TerminalTile) getTileEntity();
        if (tile.hasSwitch(pos)){
            return tile.getSwitchValue(pos);
        }else {
            return state;
        }

    }

    public static void open(BlockPos pos){
        Minecraft.getInstance().setScreen(new TerminalScreen(pos));
    }
}
