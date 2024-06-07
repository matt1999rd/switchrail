package fr.mattmouss.switchrail.other;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class PictureRenderState extends RenderState {

    public static final RenderType pictureRenderType = RenderType.create("picture_rendering",
            DefaultVertexFormats.POSITION_COLOR_LIGHTMAP,
            GL11.GL_QUADS,
            65536,false,false,
            RenderType.State.builder()
                    .setAlphaState(RenderState.DEFAULT_ALPHA)
                    .setTransparencyState(RenderState.TRANSLUCENT_TRANSPARENCY)
                    .setLayeringState(RenderState.NO_LAYERING)
                    .setLightmapState(RenderState.LIGHTMAP).createCompositeState(false));

    public static RenderType getModelRenderType(ResourceLocation location){
        return RenderType.create("picture_rendering",
                        DefaultVertexFormats.BLOCK,
                        GL11.GL_QUADS,
                2097152,true,false,
                        RenderType.State.builder()
                                .setTextureState(new TextureState(location, false, true))
                                .setTransparencyState(RenderState.NO_TRANSPARENCY)
                                .setAlphaState(RenderState.DEFAULT_ALPHA)
                                .setCullState(RenderState.CULL)
                                .setLightmapState(RenderState.LIGHTMAP)
                                .setDiffuseLightingState(RenderState.DIFFUSE_LIGHTING)
                                .createCompositeState(false));

    }

    public static RenderType getTextureRenderType(ResourceLocation location){
        return RenderType.create("texture_rendering",
                DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP,
                GL11.GL_QUADS,
                256,true,false,
                RenderType.State.builder()
                        .setTextureState(new TextureState(location, false, false))
                        .setTransparencyState(RenderState.NO_TRANSPARENCY)
                        .setAlphaState(RenderState.DEFAULT_ALPHA)
                        .setCullState(RenderState.CULL)
                        .setLightmapState(RenderState.LIGHTMAP)
                        .createCompositeState(false));
    }

    public PictureRenderState(String p_i225973_1_, Runnable p_i225973_2_, Runnable p_i225973_3_) {
        super(p_i225973_1_, p_i225973_2_, p_i225973_3_);
        throw new UnsupportedOperationException();
    }
}
