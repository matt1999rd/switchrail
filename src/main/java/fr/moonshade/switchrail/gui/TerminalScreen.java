package fr.moonshade.switchrail.gui;

import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.moonshade.switchrail.blocks.ITerminalHandler;
import fr.moonshade.switchrail.blocks.TerminalCell;
import fr.moonshade.switchrail.blocks.TerminalTile;
import fr.moonshade.switchrail.enum_rail.Corners;
import fr.moonshade.switchrail.enum_rail.RailType;
import fr.moonshade.switchrail.network.Networking;
import fr.moonshade.switchrail.network.TerminalScreenPacket;
import fr.moonshade.switchrail.other.Vector2i;
import fr.moonshade.switchrail.switchblock.Switch;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import com.mojang.datafixers.util.Pair;

import org.lwjgl.glfw.GLFW;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.ArrayList;

public class TerminalScreen extends RailScreen {
    public static final byte ADD_SWITCH_ACTION = 0;
    public static final byte REMOVE_SWITCH_ACTION = 1;
    public static final byte SET_SWITCH_ACTION = 2;
    private static final String conflictError = "Conflict Error : one of the switch is already activated by another terminal";
    private int ErrorState = 0;
    private static final int ERROR_SCREEN_WIDTH = 107;
    private static final int ERROR_SCREEN_BEGINNING_X = WIDTH - 22;
    private static final int ERROR_SCREEN_BEGINNING_Y = 77;
    private final int index; // -1 if no index is given. Bad usage to my mind but optional is forbidden

    public TerminalScreen(BlockPos pos,int index) {
        super(pos);
        this.index = index;
    }

    @Override
    protected ITerminalHandler getHandler() {
        assert this.minecraft != null;
        assert this.minecraft.level != null;
        BlockEntity tileEntity = this.minecraft.level.getBlockEntity(pos);
        if (tileEntity instanceof TerminalTile){
            return (ITerminalHandler) tileEntity;
        }else if (tileEntity instanceof PanelTile){
            PanelTile panelTile = (PanelTile) tileEntity;
            if (index == -1) throw new IllegalStateException("Try to open a screen for a tiny switch terminal while no cell position on the panel was given from the server");
            PanelCellPos cellPos = PanelCellPos.fromIndex(panelTile,index);
            IPanelCell panelCell = cellPos.getPanelTile().getIPanelCell(cellPos);
            if (panelCell instanceof TerminalCell){
                return (ITerminalHandler) panelCell;
            }
            throw new IllegalStateException("Panel cell found in the cell pos is not a terminal cell as it was expected !");

        }
        assert tileEntity != null;
        throw new IllegalStateException("BlockPos of the terminal screen ("+pos+") is not associated with a correct tile entity (found tile entity of type "+tileEntity.getClass()+" ) !");
    }

    @Override
    protected boolean isRelevantRail(RailType type) {
        return type.isSwitch();
    }

    @Override
    @ParametersAreNonnullByDefault
    public void renderBackground(PoseStack stack) {
        super.renderBackground(stack);
        assert this.minecraft != null;
        RenderSystem.setShaderTexture(0,POS_BUTTON);
        Vector2i relative = getRelative();
        this.blit(stack,relative.x+ERROR_SCREEN_BEGINNING_X,relative.y+ERROR_SCREEN_BEGINNING_Y,0,26,124,26);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        assert this.minecraft != null;
        RenderSystem.setShaderTexture(0,POS_BUTTON);
        Vector2i relative = getRelative();
        this.blit(stack,relative.x+ERROR_SCREEN_BEGINNING_X,relative.y+ERROR_SCREEN_BEGINNING_Y,0,26,124,26);
        ITerminalHandler tile = getHandler();
        if (tile.isPowered()){
            if (tile.isBlocked()){
                tile.tryUnblockTerminal();
            }else {
                tile.tryBlockTerminal();
            }
        }
        if (tile.isBlocked()){
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

    private void renderErrorWithMovement(PoseStack stack) {
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

    private void drawHoveringText(PoseStack stack,int mouseX,int mouseY){
        ArrayList<FormattedText> textLines = Lists.newArrayList();
        textLines.add(FormattedText.of("Conflict Switch", Style.EMPTY.applyFormats(ChatFormatting.BOLD, ChatFormatting.RED)));
        //Vector2i relative = getRelative();
        assert minecraft != null;
        renderComponentTooltip(stack,textLines,mouseX,mouseY,minecraft.font);
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
    public boolean onRailClicked(Pair<RailType,BlockPos> data, double mouseX, double mouseY, Vector2i relative, int button) {
        ITerminalHandler tile = getHandler();
        TerminalScreenPacket packet = null;
        BlockPos switchBlockPos = data.getSecond();
        // flag is a byte that indicates boolean used for action on set switch part
        // if flag = 0x00 -> set position of normal switch
        // if flag = 0xA1 -> set position of double slip switch
        //      if flag = 0x01 -> set position of dds knowing that right up was clicked
        //      if flag = 0x11 -> set position of dds knowing that left down was clicked
        byte flag = 0;
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT){
            //when we click on a switch with the left button of the mouse, we add or modify the switch
            if (isDisabled(switchBlockPos)){
                tile.addSwitch(switchBlockPos);
                packet = new TerminalScreenPacket(flag,pos,index,switchBlockPos, ADD_SWITCH_ACTION);
            }else {
                BlockState state = tile.getSwitchValue(switchBlockPos);
                if (!(state.getBlock() instanceof Switch)){
                    throw new IllegalStateException("Block switchBlockPos clicked is not pointing toward a switch !");
                }
                Switch switchBlock = (Switch) state.getBlock();
                EnumProperty<Corners> property = switchBlock.getSwitchPositionProperty();
                if (data.getFirst() == RailType.DOUBLE_SLIP){
                    flag += 1;
                    Direction.Axis axis = state.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                    boolean isLeftDownNearest = !isMouseAbove45degDiagonal(switchBlockPos, axis == Direction.Axis.Z, relative, mouseX, mouseY);
                    if (isLeftDownNearest)flag += 2;
                    Corners corners = state.getValue(property);
                    tile.setPosition(switchBlockPos,state.setValue(property,corners.moveDssSwitch(isLeftDownNearest)));
                }else {
                    tile.setPosition(switchBlockPos,state.cycle(property));
                }
                packet = new TerminalScreenPacket(flag,pos,index,switchBlockPos,SET_SWITCH_ACTION);
            }
        }else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT){
            //when we click on a switch with the right button of the mouse, we remove the switch if it is present
            if (!isDisabled(switchBlockPos)){
                tile.removeSwitch(switchBlockPos);
                packet = new TerminalScreenPacket(flag,pos,index,switchBlockPos,REMOVE_SWITCH_ACTION);
            }
        }
        if (packet != null)Networking.INSTANCE.sendToServer(packet);
        return true;
    }

    @Override
    public boolean isDisabled(BlockPos pos) {
        ITerminalHandler tile = getHandler();
        return !tile.hasSwitch(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        BlockState state = super.getBlockState(pos);
        ITerminalHandler tile = getHandler();
        if (tile.hasSwitch(pos)){
            return tile.getSwitchValue(pos);
        }else {
            return state;
        }

    }
}
