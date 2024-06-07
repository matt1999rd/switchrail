package fr.mattmouss.switchrail.blocks;

import com.dannyandson.tinyredstone.api.IOverlayBlockInfo;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCellInfoProvider;
import com.dannyandson.tinyredstone.blocks.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fr.mattmouss.switchrail.other.SRRenderHelper;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

public class TerminalCell implements IPanelCell, IPanelCellInfoProvider {

    private boolean isPowered = false;

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int color, float alpha) {
        IVertexBuilder builder = buffer.getBuffer((alpha == 1.0) ? RenderType.solid() : RenderType.translucent());
        // vecteur xTR,yTR,zTR ne correspond pas au vecteur classique x,y,z de minecraft
        // xTR = z
        // yTR = x
        // zTR = y
        // render base
        renderBase(builder,matrixStack,combinedLight,alpha);
        //renderBase(buffer,matrixStack,combinedLight,color,alpha);
        // vertical bar rendering
        renderVerticalBar(builder,matrixStack,combinedLight,alpha);
        // redstone point rendering
        renderRedstonePoint(builder,matrixStack,combinedLight,alpha);
    }

    public void renderBase(IVertexBuilder builder,MatrixStack stack,int combinedLight,float alpha){
        // top / up
        stack.pushPose();
        stack.translate(0,0,2/16F);
        SRRenderHelper.drawRectangle(builder,SRRenderHelper.SPRITE_TERMINAL,stack,0,1,0,1,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
        // bottom / down
        stack.pushPose();
        SRRenderHelper.rotate(stack, Direction.Axis.X,180);
        stack.translate(0,-1,0);
        SRRenderHelper.drawRectangle(builder,SRRenderHelper.SPRITE_SMOOTH_STONE,stack,0,1,0,1,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
        // back / north
        stack.pushPose();
        SRRenderHelper.rotate(stack,Direction.Axis.X,-90);
        stack.translate(0,-2/16F,1);
        SRRenderHelper.drawRectangle(builder,SRRenderHelper.SPRITE_SMOOTH_STONE,stack,0,1,0,2/16F,0,16,14,16,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
        // right / east
        stack.pushPose();
        SRRenderHelper.rotate(stack,Direction.Axis.Y,90);
        stack.translate(-2/16F,0,1);
        SRRenderHelper.drawRectangle(builder,SRRenderHelper.SPRITE_SMOOTH_STONE,stack,0,2/16F,0,1,14,16,0,16,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
        // left / west
        stack.pushPose();
        SRRenderHelper.rotate(stack, Direction.Axis.Y,-90);
        SRRenderHelper.drawRectangle(builder,SRRenderHelper.SPRITE_SMOOTH_STONE,stack,0,2/16F,0,1,16,14,0,16,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
        // front / south
        stack.pushPose();
        SRRenderHelper.rotate(stack,Direction.Axis.X,90);
        SRRenderHelper.drawRectangle(builder,SRRenderHelper.SPRITE_SMOOTH_STONE,stack,0,1,0,2/16F,0,16,16,14,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
    }

    public void renderVerticalBar(IVertexBuilder builder,MatrixStack stack,int combinedLight,float alpha){
        // back / north
        stack.pushPose();
        SRRenderHelper.rotate(stack,Direction.Axis.X,-90);
        stack.translate(0,-14/16F,2/16F); //translation pour la taille du block
        stack.translate(7/16F,-2/16F,7/16F); //translation pour la position du block
        SRRenderHelper.drawRectangle(builder,SRRenderHelper.SPRITE_METAL,stack,0,2/16F,0,14/16F,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
        // right / east
        stack.pushPose();
        SRRenderHelper.rotate(stack,Direction.Axis.Y,90);
        stack.translate(-14/16F,0,2/16F);
        stack.translate(-2/16F,7/16F,7/16F);
        SRRenderHelper.drawRectangle(builder,SRRenderHelper.SPRITE_METAL,stack,0,14/16F,0,2/16F,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
        // left / west
        stack.pushPose();
        SRRenderHelper.rotate(stack, Direction.Axis.Y,-90);
        stack.translate(2/16F,7/16F,-7/16F);
        SRRenderHelper.drawRectangle(builder,SRRenderHelper.SPRITE_METAL,stack,0,14/16F,0,2/16F,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
        // front / south
        stack.pushPose();
        SRRenderHelper.rotate(stack,Direction.Axis.X,90);
        stack.translate(7/16F,2/16F,-7/16F);
        SRRenderHelper.drawRectangle(builder,SRRenderHelper.SPRITE_METAL,stack,0,2/16F,0,14/16F,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
    }

    public void renderRedstonePoint(IVertexBuilder builder,MatrixStack stack,int combinedLight,float alpha){
        TextureAtlasSprite spriteRedstone = isPowered ? SRRenderHelper.SPRITE_REDSTONE_ON : SRRenderHelper.SPRITE_REDSTONE_OFF;
        // top / up
        stack.pushPose();
        stack.translate(0,0,5/16F);
        stack.translate(6/16F,6/16F,12/16F);
        SRRenderHelper.drawRectangle(builder,spriteRedstone,stack,0,4/16F,0,4/16F,0,0,1,1,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
        // bottom / down
        stack.pushPose();
        SRRenderHelper.rotate(stack, Direction.Axis.X,180);
        stack.translate(0,-4/16F,0);
        stack.translate(6/16F,-6/16F,-12/16F);
        SRRenderHelper.drawRectangle(builder,spriteRedstone,stack,0,4/16F,0,4/16F,0,0,1,1,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
        // back / north
        stack.pushPose();
        SRRenderHelper.rotate(stack,Direction.Axis.X,-90);
        stack.translate(0,-5/16F,1);
        stack.translate(6/16F,-12/16F,-6/16F);
        SRRenderHelper.drawRectangle(builder,spriteRedstone,stack,0,4/16F,0,5/16F,0,4,0,2,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
        // right / east
        stack.pushPose();
        SRRenderHelper.rotate(stack,Direction.Axis.Y,90);
        stack.translate(-5/16F,0,1);
        stack.translate(-12/16F,6/16F,-6/16F);
        SRRenderHelper.drawRectangle(builder,spriteRedstone,stack,0,5/16F,0,4/16F,0,4,0,2,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
        // left / west
        stack.pushPose();
        SRRenderHelper.rotate(stack, Direction.Axis.Y,-90);
        stack.translate(12/16F,6/16F,-6/16F);
        SRRenderHelper.drawRectangle(builder,spriteRedstone,stack,0,5/16F,0,4/16F,0,4,0,2,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
        // front / south
        stack.pushPose();
        SRRenderHelper.rotate(stack,Direction.Axis.X,90);
        stack.translate(6/16F,12/16F,-6/16F);
        SRRenderHelper.drawRectangle(builder,spriteRedstone,stack,0,4/16F,0,5/16F,0,4,0,2,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
    }

    @Override
    public boolean onPlace(PanelCellPos cellPos, PlayerEntity player) {
        return IPanelCell.super.onPlace(cellPos, player);
    }

    @Override
    public boolean neighborChanged(PanelCellPos cellPos) {
        PanelCellNeighbor commandNeighbor = cellPos.getNeighbor(Side.RIGHT);
        if (commandNeighbor != null){
            isPowered = commandNeighbor.getStrongRsOutput()>0 || commandNeighbor.getWeakRsOutput()>0;
        }
        return false;
    }

    @Override
    public int getWeakRsOutput(Side side) {
        return 0;
    }

    @Override
    public int getStrongRsOutput(Side side) {
        return 0;
    }

    @Override
    public CompoundNBT writeNBT() {
        return new CompoundNBT();
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {

    }

    @Override
    public void addInfo(IOverlayBlockInfo iOverlayBlockInfo, PanelTile panelTile, PosInPanelCell posInPanelCell) {

    }
}
