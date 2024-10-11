package fr.moonshade.switchrail.other;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;


public class PictureRenderState extends RenderStateShard {

    public static final RenderType pictureRenderType = RenderType.create("picture_rendering",
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            65536,false,false,
            RenderType.CompositeState.builder()
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setLayeringState(RenderStateShard.NO_LAYERING)
                    .setLightmapState(RenderStateShard.LIGHTMAP).createCompositeState(false));

    public static RenderType getModelRenderType(ResourceLocation location){
        return RenderType.create("picture_rendering",
                        DefaultVertexFormat.BLOCK,
                        VertexFormat.Mode.QUADS,
                2097152,true,false,
                        RenderType.CompositeState.builder()
                                .setTextureState(new TextureStateShard(location, false, true))
                                .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                                .setCullState(RenderStateShard.CULL)
                                .setLightmapState(RenderStateShard.LIGHTMAP)
                                .createCompositeState(false));

    }

    public static RenderType getTextureRenderType(ResourceLocation location){
        return RenderType.create("texture_rendering",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS,
                256,true,false,
                RenderType.CompositeState.builder()
                        .setTextureState(new TextureStateShard(location, false, false))
                        .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                        .setCullState(RenderStateShard.CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .createCompositeState(false));
    }

    public PictureRenderState(String p_i225973_1_, Runnable p_i225973_2_, Runnable p_i225973_3_) {
        super(p_i225973_1_, p_i225973_2_, p_i225973_3_);
        throw new UnsupportedOperationException();
    }
}
