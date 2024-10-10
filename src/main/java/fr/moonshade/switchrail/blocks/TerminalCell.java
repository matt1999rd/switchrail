package fr.moonshade.switchrail.blocks;

import com.dannyandson.tinyredstone.api.IOverlayBlockInfo;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCellInfoProvider;
import com.dannyandson.tinyredstone.blocks.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fr.moonshade.switchrail.network.Networking;
import fr.moonshade.switchrail.network.OpenTerminalScreenPacket;
import fr.moonshade.switchrail.other.SRRenderHelper;
import fr.moonshade.switchrail.other.TerminalStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkDirection;

import java.util.function.Supplier;

public class TerminalCell implements IPanelCell, IPanelCellInfoProvider, ITerminalHandler, ISRCell {

    private boolean isPowered = false;
    private boolean isBlocked = false;
    private PanelCellPos cellPos; // This variable is updated only server-side
    // This two next field are null server-side and non null client-side
    private BlockPos pos;
    private int cellPosIndex;

    private final Supplier<IllegalArgumentException> storageErrorSupplier = () -> new IllegalArgumentException("no storage found in Terminal Tile Entity !");
    private final LazyOptional<TerminalStorage> storage= LazyOptional.of(TerminalStorage::new);


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

    @Override
    public boolean tick(PanelCellPos cellPos) {
        this.cellPos = cellPos;
        return onTick();
    }

    public void renderBase(IVertexBuilder builder, MatrixStack stack, int combinedLight, float alpha){
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
    public boolean needsSolidBase() {
        return true;
    }

    @Override
    public boolean onPlace(PanelCellPos cellPos, PlayerEntity player) {
        isPowered = false;
        this.cellPos = cellPos;
        storage.ifPresent(terminalStorage -> terminalStorage.setBasePos(cellPos.getPanelTile().getBlockPos()));
        return IPanelCell.super.onPlace(cellPos, player);
    }

    @Override
    public boolean neighborChanged(PanelCellPos cellPos) {
        PanelCellNeighbor commandNeighbor = cellPos.getNeighbor(Side.RIGHT);
        if ( (commandNeighbor != null ? commandNeighbor.getWeakRsOutput() : 0)  <= 0) {
            if (this.isPowered) {
                this.isPowered = false;
                this.actionOnUnpowered();
                return true;
            }
        } else if (!this.isPowered) {
            this.isPowered = true;
            this.actionOnPowered();
            return true;
        }
        return false;
    }

    @Override
    public boolean onBlockActivated(PanelCellPos cellPos, PanelCellSegment segmentClicked, PlayerEntity player) {
        World world = cellPos.getPanelTile().getLevel();
        assert world != null;
        if (!world.isClientSide){
            //send a packet to client to open screen
            Networking.INSTANCE.sendTo(new OpenTerminalScreenPacket(cellPos),((ServerPlayerEntity) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
        return true;
    }

    @Override
    public boolean hasActivation() {
        return true;
    }

    @Override
    public void onRemove(PanelCellPos cellPos) {
        if (!isBlocked() && isPowered()) {
            this.freeAllSwitch();
        }
        IPanelCell.super.onRemove(cellPos);
    }

    @Override
    public int getWeakRsOutput(Side side) {
        return getStrongRsOutput(side);
    }

    @Override
    public int getStrongRsOutput(Side side) {
        if (side == Side.LEFT && !isBlocked() && isPowered()){
            return 15;
        }
        return 0;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT compoundNBT = new CompoundNBT();
        compoundNBT.putBoolean("powered",isPowered);
        compoundNBT.putBoolean("blocked",isBlocked);
        CompoundNBT storageNBT = storage.map(TerminalStorage::serializeNBT).orElseThrow(getErrorSupplier());
        compoundNBT.put("storage",storageNBT);
        if (cellPos != null){ // we are server-side
            compoundNBT.putLong("panelpos",cellPos.getPanelTile().getBlockPos().asLong());
            compoundNBT.putInt("cellpos",cellPos.getIndex());
        }
        return compoundNBT;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
        isPowered = compoundNBT.getBoolean("powered");
        isBlocked = compoundNBT.getBoolean("blocked");
        CompoundNBT storageNBT = compoundNBT.getCompound("storage");
        storage.ifPresent(terminalStorage -> terminalStorage.deserializeNBT(storageNBT));
        if (compoundNBT.contains("panelpos")){ // we are client-side
            pos = BlockPos.of(compoundNBT.getLong("panelpos"));
            cellPosIndex = compoundNBT.getInt("cellpos");
        }
    }

    @Override
    public void addInfo(IOverlayBlockInfo iOverlayBlockInfo, PanelTile panelTile, PosInPanelCell posInPanelCell) {

    }

    @Override
    public Supplier<IllegalArgumentException> getErrorSupplier() {
        return storageErrorSupplier;
    }

    @Override
    public LazyOptional<TerminalStorage> getTerminalStorage() {
        return storage;
    }

    @Override
    public boolean isPowered() {
        return isPowered;
    }

    @Override
    public boolean isBlocked() {
        return isBlocked;
    }

    @Override
    public TileEntity getTile() {
        if (cellPos != null){
            return cellPos.getPanelTile();
        }else if (pos != null){
            assert Minecraft.getInstance().level != null; //useless here because we are client-side
            return Minecraft.getInstance().level.getBlockEntity(pos);
        }
        throw new IllegalStateException("The cell has no panel pos nor cell pos defined !");
    }

    @Override
    public void setBlockedFlag(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    @Override
    public BlockPos getPanelPos() {
        return pos;
    }

    @Override
    public int getCellIndex() {
        return cellPosIndex;
    }
}
