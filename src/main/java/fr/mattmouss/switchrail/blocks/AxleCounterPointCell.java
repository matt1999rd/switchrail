package fr.mattmouss.switchrail.blocks;

import com.dannyandson.tinyredstone.api.IOverlayBlockInfo;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCellInfoProvider;
import com.dannyandson.tinyredstone.blocks.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fr.mattmouss.switchrail.other.CounterStorage;
import fr.mattmouss.switchrail.other.SRRenderHelper;
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

import java.util.function.Supplier;

public class AxleCounterPointCell implements IPanelCell,ICounterHandler,ICounterPoint, ISRCell, IPanelCellInfoProvider {

    private boolean isPowered = true;
    private PanelCellPos cellPos; // This variable is updated only server-side
    // This two next field are null server-side and non-null client-side
    private BlockPos pos;
    private int cellPosIndex;
    private final Supplier<IllegalArgumentException> storageErrorSupplier = () -> new IllegalArgumentException("no storage found in Axle Counter Cell !");
    private final LazyOptional<CounterStorage> storage = LazyOptional.of(CounterStorage::new);


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
        // redstone point rendering
        renderRedstonePoint(builder,matrixStack,combinedLight,alpha);
    }

    @Override
    public boolean neighborChanged(PanelCellPos panelCellPos) {
        return false;
    }

    private void renderRedstonePoint(IVertexBuilder builder, MatrixStack stack, int combinedLight, float alpha) {
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

    private void renderBase(IVertexBuilder builder, MatrixStack stack, int combinedLight, float alpha) {
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
    public boolean onPlace(PanelCellPos cellPos, PlayerEntity player) {
        this.cellPos = cellPos;
        storage.ifPresent(counterStorage -> counterStorage.setBasePos(cellPos.getPanelTile().getBlockPos()));
        return IPanelCell.super.onPlace(cellPos, player);
    }

    @Override
    public void onRemove(PanelCellPos cellPos) {
        if (this.cellPos != null){
            // this function is done only server side
            World world = this.cellPos.getPanelTile().getLevel();
            assert world != null;
            this.onACRemove(world,cellPos.getPanelTile().getBlockPos());
        }
    }

    @Override
    public boolean onBlockActivated(PanelCellPos cellPos, PanelCellSegment segmentClicked, PlayerEntity player) {
        World world = cellPos.getPanelTile().getLevel();
        assert world != null;
        if (!world.isClientSide){
            ICounterPoint.super.onBlockClicked(world,this.cellPos.getPanelTile().getBlockPos(), (ServerPlayerEntity) player, this.cellPos.getIndex());
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
    public CompoundNBT writeNBT() {
        CompoundNBT compoundNBT = new CompoundNBT();
        compoundNBT.putBoolean("powered",isPowered);
        CompoundNBT storageNBT = storage.map(CounterStorage::serializeNBT).orElseThrow(getErrorSupplier());
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
        CompoundNBT storageNBT = compoundNBT.getCompound("storage");
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
    public void setPowered(boolean powered) {
        isPowered = powered;
        if (this.cellPos != null){
            //server side
            this.cellPos.getPanelTile().updateNeighborCells(this.cellPos);
        }
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
    public void addInfo(IOverlayBlockInfo iOverlayBlockInfo, PanelTile panelTile, PosInPanelCell posInPanelCell) {

    }
}
