package fr.mattmouss.switchrail.blocks;

import com.dannyandson.tinyredstone.api.IOverlayBlockInfo;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCellInfoProvider;
import com.dannyandson.tinyredstone.blocks.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.mattmouss.switchrail.network.Networking;
import fr.mattmouss.switchrail.network.OpenTerminalScreenPacket;
import fr.mattmouss.switchrail.other.SRRenderHelper;
import fr.mattmouss.switchrail.other.TerminalStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fmllegacy.network.NetworkDirection;

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
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int color, float alpha) {
        VertexConsumer builder = buffer.getBuffer((alpha == 1.0) ? RenderType.solid() : RenderType.translucent());
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

    @Override
    public Side getBaseSide() {
        return Side.BOTTOM;
    }

    public void renderBase(VertexConsumer builder, PoseStack stack, int combinedLight, float alpha){
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

    public void renderVerticalBar(VertexConsumer builder,PoseStack stack,int combinedLight,float alpha){
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

    public void renderRedstonePoint(VertexConsumer builder,PoseStack stack,int combinedLight,float alpha){
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
    public boolean onPlace(PanelCellPos cellPos, Player player) {
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
    public boolean onBlockActivated(PanelCellPos cellPos, PanelCellSegment segmentClicked, Player player) {
        Level world = cellPos.getPanelTile().getLevel();
        assert world != null;
        if (!world.isClientSide){
            //send a packet to client to open screen
            Networking.INSTANCE.sendTo(new OpenTerminalScreenPacket(cellPos),((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
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
    public CompoundTag writeNBT() {
        CompoundTag compoundNBT = new CompoundTag();
        compoundNBT.putBoolean("powered",isPowered);
        compoundNBT.putBoolean("blocked",isBlocked);
        CompoundTag storageNBT = storage.map(TerminalStorage::serializeNBT).orElseThrow(getErrorSupplier());
        compoundNBT.put("storage",storageNBT);
        if (cellPos != null){ // we are server-side
            compoundNBT.putLong("panelpos",cellPos.getPanelTile().getBlockPos().asLong());
            compoundNBT.putInt("cellpos",cellPos.getIndex());
        }
        return compoundNBT;
    }

    @Override
    public void readNBT(CompoundTag compoundNBT) {
        isPowered = compoundNBT.getBoolean("powered");
        isBlocked = compoundNBT.getBoolean("blocked");
        CompoundTag storageNBT = compoundNBT.getCompound("storage");
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
    public BlockEntity getTile() {
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
