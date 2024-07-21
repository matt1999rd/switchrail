package fr.mattmouss.switchrail.blocks;

import com.dannyandson.tinyredstone.PanelOverflowException;
import com.dannyandson.tinyredstone.api.IOverlayBlockInfo;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCellInfoProvider;
import com.dannyandson.tinyredstone.blocks.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.mattmouss.switchrail.other.CounterStorage;
import fr.mattmouss.switchrail.other.SRRenderHelper;
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

import java.util.function.Supplier;

public class AxleCounterPointCell implements IPanelCell,ICounterHandler,ICounterPoint, ISRCell, IPanelCellInfoProvider {

    private boolean isPowered = true;
    private PanelCellPos cellPos; // This variable is updated only server-side
    // This two next field are null server-side and non-null client-side
    private BlockPos pos;
    private int cellPosIndex;
    private final Supplier<IllegalArgumentException> storageErrorSupplier = () -> new IllegalArgumentException("no storage found in Axle Counter Cell !");
    private final LazyOptional<CounterStorage> storage = LazyOptional.of(CounterStorage::new);
    private boolean isDirty = false;


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
        // redstone point rendering
        renderRedstonePoint(builder,matrixStack,combinedLight,alpha);
    }

    public void markDirty(){
        isDirty = true;
    }

    @Override
    public boolean neighborChanged(PanelCellPos panelCellPos) {
        return false;
    }

    @Override
    public boolean isIndependentState() {
        return true;
    }

    private void renderRedstonePoint(VertexConsumer builder, PoseStack stack, int combinedLight, float alpha) {
        TextureAtlasSprite spriteRedstone = isPowered ? SRRenderHelper.SPRITE_REDSTONE_ON : SRRenderHelper.SPRITE_REDSTONE_OFF;
        // top / up
        stack.pushPose();
        stack.translate(0,0,2/16F);
        stack.translate(7/16F,7/16F,2/16F);
        SRRenderHelper.drawRectangle(builder,spriteRedstone,stack,0,2/16F,0,2/16F,0,2,0,2,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
        // back / north
        stack.pushPose();
        SRRenderHelper.rotate(stack,Direction.Axis.X,-90);
        stack.translate(0,-2/16F,1);
        stack.translate(7/16F,-2/16F,-7/16F);
        SRRenderHelper.drawRectangle(builder,spriteRedstone,stack,0,2/16F,0,2/16F,0,2,0,2,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
        // right / east
        stack.pushPose();
        SRRenderHelper.rotate(stack,Direction.Axis.Y,90);
        stack.translate(-2/16F,0,1);
        stack.translate(-2/16F,7/16F,-7/16F);
        SRRenderHelper.drawRectangle(builder,spriteRedstone,stack,0,2/16F,0,2/16F,0,2,0,2,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
        // left / west
        stack.pushPose();
        SRRenderHelper.rotate(stack, Direction.Axis.Y,-90);
        stack.translate(2/16F,7/16F,-7/16F);
        SRRenderHelper.drawRectangle(builder,spriteRedstone,stack,0,2/16F,0,2/16F,0,2,0,2,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
        // front / south
        stack.pushPose();
        SRRenderHelper.rotate(stack,Direction.Axis.X,90);
        stack.translate(7/16F,2/16F,-7/16F);
        SRRenderHelper.drawRectangle(builder,spriteRedstone,stack,0,2/16F,0,2/16F,0,2,0,2,combinedLight,0xFFFFFFFF,alpha);
        stack.popPose();
    }

    private void renderBase(VertexConsumer builder, PoseStack stack, int combinedLight, float alpha) {
        // top / up
        stack.pushPose();
        stack.translate(0,0,2/16F);
        SRRenderHelper.drawRectangle(builder,SRRenderHelper.SPRITE_AXLE_COUNTER,stack,0,1,0,1,combinedLight,0xFFFFFFFF,alpha);
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

    @Override
    public boolean onPlace(PanelCellPos cellPos, Player player) {
        this.cellPos = cellPos;
        storage.ifPresent(counterStorage -> counterStorage.setBasePos(cellPos.getPanelTile().getBlockPos()));
        return IPanelCell.super.onPlace(cellPos, player);
    }

    @Override
    public void onRemove(PanelCellPos cellPos) {
        if (this.cellPos != null){
            // this function is done only server side
            Level world = this.cellPos.getPanelTile().getLevel();
            assert world != null;
            this.onACRemove(world,cellPos.getPanelTile().getBlockPos(),cellPos.getIndex());
        }
    }

    @Override
    public boolean onBlockActivated(PanelCellPos cellPos, PanelCellSegment segmentClicked, Player player) {
        Level world = cellPos.getPanelTile().getLevel();
        assert world != null;
        if (!world.isClientSide){
            ICounterPoint.super.onBlockClicked(world,this.cellPos.getPanelTile().getBlockPos(), (ServerPlayer) player, this.cellPos.getIndex());
        }
        return true;
    }

    @Override
    public boolean hasActivation() {
        return true;
    }

    @Override
    public boolean tick(PanelCellPos cellPos) {
        this.cellPos = cellPos;
        if (isDirty){
            isDirty = false;
            return true;
        }
        return false;
    }

    @Override
    public int getWeakRsOutput(Side side) {
        return getStrongRsOutput(side);
    }

    @Override
    public int getStrongRsOutput(Side side) {
        if (isPowered){
            return 15;
        }
        return 0;
    }

    @Override
    public Side getBaseSide() {
        return Side.BOTTOM;
    }

    @Override
    public CompoundTag writeNBT() {
        CompoundTag compoundNBT = new CompoundTag();
        compoundNBT.putBoolean("powered",isPowered);
        CompoundTag storageNBT = storage.map(CounterStorage::serializeNBT).orElseThrow(getErrorSupplier());
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
        CompoundTag storageNBT = compoundNBT.getCompound("storage");
        storage.ifPresent(counterStorage -> counterStorage.deserializeNBT(storageNBT));
        if (compoundNBT.contains("panelpos")){ // we are client-side
            pos = BlockPos.of(compoundNBT.getLong("panelpos"));
            cellPosIndex = compoundNBT.getInt("cellpos");
        }
    }

    @Override
    public LazyOptional<CounterStorage> getCounterStorage() {
        return storage;
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
    public void setPowered(boolean powered) {
        isPowered = powered;
        markDirty();
    }

    @Override
    public boolean needsSolidBase() {
        return true;
    }

    @Override
    public Supplier<IllegalArgumentException> getErrorSupplier() {
        return storageErrorSupplier;
    }

    @Override
    public BlockPos getPanelPos() {
        return pos;
    }

    @Override
    public int getCellIndex() {
        return cellPosIndex;
    }

    @Override
    public void addInfo(IOverlayBlockInfo overlayBlockInfo, PanelTile panelTile, PosInPanelCell posInPanelCell) {
        overlayBlockInfo.setPowerOutput(this.isPowered ? 15 : 0);
        overlayBlockInfo.addInfo("Axle number : "+getAxle());
    }
}
