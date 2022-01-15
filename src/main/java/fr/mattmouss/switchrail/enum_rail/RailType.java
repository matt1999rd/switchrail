package fr.mattmouss.switchrail.enum_rail;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.mattmouss.switchrail.blocks.ControllerBlock;
import fr.mattmouss.switchrail.blocks.CrossedRail;
import fr.mattmouss.switchrail.other.Util;
import fr.mattmouss.switchrail.switchblock.*;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;

import java.util.function.Predicate;

public enum RailType {
    CROSSED_RAIL(10,CrossedRail.class),
    CONTROLLER_BLOCK(11,ControllerBlock.class),
    SIMPLE(12, SwitchStraight.class),
    Y(28,SwitchDoubleTurn.class),
    THREE_WAY(36,SwitchTriple.class),
    SINGLE_SLIP(48, SwitchSimpleSlip.class),
    DOUBLE_SLIP(56, SwitchDoubleSlip.class),
    RAIL(0,AbstractRailBlock.class);
    final int shift;
    final Class<? extends Block> instanceClass;
    static final Predicate<Property<?>> hasCornerProperty = property -> property.getValueClass() == Corners.class && property instanceof EnumProperty;
    final Vector2f uvDimension = Util.makeVector(32F/256F);


    RailType(int shift, Class<? extends Block> instanceClass){
        this.shift = shift;
        this.instanceClass = instanceClass;
    }

    public static RailType getType(Block block){
        for (RailType switchType : RailType.values()){
            Class<?> class_in = (switchType == RAIL)? (Class<?>) block.getClass().getGenericSuperclass() :block.getClass();
            if (class_in == switchType.instanceClass) {
                return switchType;
            }
        }
        return null;
    }

    public boolean isSwitch(){
        return this != CROSSED_RAIL && this != CONTROLLER_BLOCK && this != RAIL;
    }

    public int getUVShift(BlockState state){
        int bs_shift = 0;
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && this != CONTROLLER_BLOCK){
            Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            bs_shift+=dir.get2DDataValue();
        }
        if (state.hasProperty(BlockStateProperties.RAIL_SHAPE)){
            RailShape shape = state.getValue(BlockStateProperties.RAIL_SHAPE);
            bs_shift+=shape.ordinal();
        }
        if (state.hasProperty(BlockStateProperties.DOOR_HINGE)){
            DoorHingeSide side = state.getValue(BlockStateProperties.DOOR_HINGE);
            bs_shift+=side.ordinal()*8;
        }
        if (this == DOUBLE_SLIP){
            if (!(state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS))){
                throw new IllegalStateException(
                        "Type called is not matching the block state given in argument : expect tjd got "
                                +state.getBlock().getRegistryName()+" instead" );
            }
            Direction.Axis dir_axis = state.getValue(BlockStateProperties.HORIZONTAL_AXIS);
            // when replacing axis we have to map the NORTH part to the Z axis and the EAST part to the X axis
            // this leads to use the ordinal and map 0 to 1 and 2 to 0 -> y=ax+b
            // y(0) = 1 -> b = 1 ; y(2) = 0 -> 2a + 1 = 0 -> a = -1/2
            // y = 1 - x/2
            bs_shift+=1-dir_axis.ordinal()/2;
        }

        EnumProperty<Corners> cornersProperty = getCornerProperty(state);
        if (cornersProperty != null){
            Object[] possibleCorner = cornersProperty.getPossibleValues().stream().sorted().toArray();
            for (int i=0;i<possibleCorner.length;i++){
                if (possibleCorner[i] == state.getValue(cornersProperty)){
                    // include dss in this condition forces to have an intern condition on the possible value
                    // only dss has the four possibilities
                    bs_shift += i*(possibleCorner.length == 4 ? 2 : 4);
                }
            }
        }
        return bs_shift+this.shift;
    }

    private static EnumProperty<Corners> getCornerProperty(BlockState state){
        if (state.getBlock() instanceof Switch){
            return ((Switch)state.getBlock()).getSwitchPositionProperty();
        }
        return null;
    }

    public void render(MatrixStack stack, Vector2f posOnBoard, Vector2f iconDimension, BlockState blockState,boolean isEnable){
        int uvShift = this.getUVShift(blockState);
        Vector2f uvOrigin = Util.directMult(new Vector2f(uvShift%8,uvShift/8),uvDimension);
        renderQuad(stack,posOnBoard, Util.add(posOnBoard,iconDimension),uvOrigin,Util.add(uvOrigin,uvDimension),isEnable);
    }

    private void renderQuad(MatrixStack stack, Vector2f origin, Vector2f end, Vector2f uvOrigin, Vector2f uvEnd,boolean isEnable){
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        Matrix4f matrix4f = stack.last().pose();
        float colorMask = (isEnable) ? 1.0F : 0.5F;
        RenderSystem.color3f(colorMask,colorMask,colorMask);
        bufferbuilder.vertex(matrix4f, origin.x, origin.y, (float)0).uv(uvOrigin.x, uvOrigin.y).endVertex();
        bufferbuilder.vertex(matrix4f, origin.x, end.y, (float)0).uv(uvOrigin.x, uvEnd.y).endVertex();
        bufferbuilder.vertex(matrix4f, end.x, end.y, (float)0).uv(uvEnd.x, uvEnd.y).endVertex();
        bufferbuilder.vertex(matrix4f, end.x, origin.y, (float)0).uv(uvEnd.x, uvOrigin.y).endVertex();
        tessellator.end();
        RenderSystem.color3f(1.0F,1.0F,1.0F);
    }

}
