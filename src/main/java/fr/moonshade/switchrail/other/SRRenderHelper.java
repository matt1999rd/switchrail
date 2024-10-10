package fr.moonshade.switchrail.other;

import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fr.moonshade.switchrail.SwitchRailMod;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

// Class used for the rendering of tiny blocks
public class SRRenderHelper {
    public static TextureAtlasSprite SPRITE_REDSTONE_OFF = getSprite("redstone_off");
    public static TextureAtlasSprite SPRITE_REDSTONE_ON = getSprite("redstone_on");
    public static TextureAtlasSprite SPRITE_METAL = getSprite("metal");
    public static TextureAtlasSprite SPRITE_SMOOTH_STONE = getSprite("smooth_stone");
    public static TextureAtlasSprite SPRITE_AXLE_COUNTER = getSprite("axle_counter");
    public static TextureAtlasSprite SPRITE_TERMINAL = getSprite("terminal");

    public static TextureAtlasSprite getSprite(String textureName)
    {
        return RenderHelper.getSprite(new ResourceLocation(SwitchRailMod.MOD_ID,"block/"+textureName));
    }

    //useful for the rotation of matrix stack -> rotate around an axis

    public static void rotate(MatrixStack stack, Direction.Axis axis, float angle){
        Vector3f vector3f = (axis == Direction.Axis.X) ? Vector3f.XP :
                (axis == Direction.Axis.Y) ? Vector3f.YP : Vector3f.ZP;
        stack.mulPose(vector3f.rotationDegrees(angle));
    }

    //A function to draw a rectangle with specific UV mapping
    public static void drawRectangle(IVertexBuilder builder,TextureAtlasSprite sprite, MatrixStack stack, float x1, float x2, float y1, float y2,float u0,float u1,float v0,float v1,int combinedLight,int color,float alpha) {
        if (u0<0 || u0>16 || u1<0 || u1>16 || v0<0 || v0>16 || v1<0 || v1>16)throw new IllegalArgumentException("Expect UV parameter with value between 0 and 1");
        RenderHelper.drawRectangle(builder,stack,x1,x2,y1,y2,sprite.getU(u0), sprite.getU(u1), sprite.getV(v0), sprite.getV(v1),combinedLight,color,alpha);
    }

    //A simpler function to automatically make the UV mapping
    public static void drawRectangle(IVertexBuilder builder,TextureAtlasSprite sprite, MatrixStack stack, float x1, float x2, float y1, float y2,int combinedLight,int color,float alpha) {
        drawRectangle(builder,sprite,stack,x1,x2,y1,y2,0, 16F,0,16F,combinedLight,color,alpha);
    }


}
