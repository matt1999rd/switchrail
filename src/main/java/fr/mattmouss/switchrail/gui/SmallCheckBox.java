package fr.mattmouss.switchrail.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.mattmouss.switchrail.SwitchRailMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class SmallCheckBox extends CheckboxButton {


    Runnable onToggle;
    private final ResourceLocation POS_BUTTON = new ResourceLocation(SwitchRailMod.MOD_ID,"textures/gui/posbutton.png");

    public SmallCheckBox(int x, int y, String text,Runnable onToggle) {
        super(x, y, 10, 10, ITextComponent.nullToEmpty(text), false);
        this.onToggle = onToggle;
    }

    @Override
    public void renderButton(MatrixStack stack, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bind(POS_BUTTON);
        RenderSystem.enableDepthTest();
        FontRenderer fontrenderer = minecraft.font;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        int offsetSelected = this.selected() ? 10 : 0;
        int offsetHovered = this.isHovered ? 10 : 0;
        blit(stack, this.x, this.y, 92+offsetSelected, 78+offsetHovered, 10, 10);
        drawString(stack, fontrenderer, this.getMessage(), this.x + 10, this.y + (this.height - 8) / 2, 14737632 | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }

    @Override
    public void onPress() {
        super.onPress();
        this.onToggle.run();
    }
}
