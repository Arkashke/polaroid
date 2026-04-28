package net.minecraft.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;

import java.awt.*;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import polaroid.client.Polaroid;
import polaroid.client.modules.api.ModuleRegistry;
import polaroid.client.modules.impl.render.InterFace;
import polaroid.client.modules.impl.render.Theme;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.utils.math.Vector4i;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.KawaseBlur;
import polaroid.client.utils.render.font.Fonts;

public abstract class Widget extends AbstractGui implements IRenderable, IGuiEventListener {
    public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    protected int width;
    protected int height;
    public int x;
    public int y;
    private ITextComponent message;
    private boolean wasHovered;
    protected boolean isHovered;
    public boolean active = true;
    public boolean visible = true;
    protected float alpha = 1.0F;
    protected long nextNarration = Long.MAX_VALUE;
    private boolean focused;

    public Widget(int x, int y, int width, int height, ITextComponent title) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.message = title;
    }

    public int getHeightRealms() {
        return this.height;
    }

    protected int getYImage(boolean isHovered) {
        int i = 1;

        if (!this.active) {
            i = 0;
        } else if (isHovered) {
            i = 2;
        }

        return i;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            if (!this.isHovered) this.focused = false;
            if (this.wasHovered != this.isHovered()) {
                if (this.isHovered()) {
                    if (this.focused) {
                        this.queueNarration(200);
                    } else {
                        this.queueNarration(750);
                    }
                } else {
                    this.nextNarration = Long.MAX_VALUE;
                }
            }

            if (this.visible) {
                this.renderButton(matrixStack, mouseX, mouseY, partialTicks);
            }

            this.narrate();
            this.wasHovered = this.isHovered();
        }
    }

    protected void narrate() {
        if (this.active && this.isHovered() && Util.milliTime() > this.nextNarration) {
            String s = this.getNarrationMessage().getString();

            if (!s.isEmpty()) {
                NarratorChatListener.INSTANCE.say(s);
                this.nextNarration = Long.MAX_VALUE;
            }
        }
    }

    protected IFormattableTextComponent getNarrationMessage() {
        return new TranslationTextComponent("gui.narrate.button", this.getMessage());
    }

    private float btnDarkness = 0.4F;
    private float hoverProgress = 0f;
    private final float animationSpeed = 0.22f;
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean isMouseHover = MathUtil.isHovered(mouseX, mouseY, (int) x, y, width, height);
        btnDarkness = (float) MathHelper.lerp(btnDarkness, isMouseHover ? 0.75F : 0.25F, 10);
        int color = isMouseHover ? ColorUtils.rgb(255, 255, 255) : ColorUtils.rgba(222, 222, 222, (int) (255));
        float darkness = this.active ? btnDarkness : 0.2F;
        int color1 = ColorUtils.getColor(color);
        int color2 = ColorUtils.getColor(color);
        int color3 = ColorUtils.getColor(color);
        int color4 = ColorUtils.getColor(color);
        int megaradiun = (int) 6f;
        if (isMouseHover) {
            hoverProgress = Math.min(hoverProgress + animationSpeed, 1f);
        } else {
            hoverProgress = Math.max(hoverProgress - animationSpeed, 0f);
            if (hoverProgress < 0.02f) hoverProgress = 0f;
        }
        int glowAlpha = (int) (50 * hoverProgress);
        int colorrect1 = ColorUtils.setAlpha(Theme.getColor(0), glowAlpha);
        int colorrect2 = ColorUtils.setAlpha(Theme.getColor(0), glowAlpha);
        int colorrect3 = ColorUtils.setAlpha(Theme.getColor(0), glowAlpha);
        int colorrect4 = ColorUtils.setAlpha(Theme.getColor(0), glowAlpha);

        DisplayUtils.drawRoundedRect(x - 2.5f, y + 2 - 2.5f, width, height ,new Vector4f(megaradiun,megaradiun,megaradiun,megaradiun),ColorUtils.rgba(15, 15, 15, 210));
        DisplayUtils.drawRoundedRectOutline(x - 2.5f, y + 2 - 2.5f, width, height,megaradiun,1.5f,ColorUtils.setAlpha(Theme.getColor(0), 50));
        DisplayUtils.drawRoundedRect(x - 2.5f - 0.5f, y + 2 - 2.5f - 0.5f, width + 1, height + 1,new Vector4f(megaradiun,megaradiun,megaradiun,megaradiun),new Vector4i(colorrect1, colorrect2, colorrect3, colorrect4));

        Fonts.otwindowsa.drawCenteredText(matrixStack, getMessage().getString(), x - 2 + width / 2, y - 3 + height / 2f - 2f - 0.5f, color, 9f);
    }

    protected void renderBg(MatrixStack matrixStack, Minecraft minecraft, int mouseX, int mouseY) {
    }

    public void onClick(double mouseX, double mouseY) {
    }

    public void onRelease(double mouseX, double mouseY) {
    }

    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible) {
            if (this.isValidClickButton(button)) {
                boolean flag = this.clicked(mouseX, mouseY);

                if (flag) {
                    this.playDownSound(Minecraft.getInstance().getSoundHandler());
                    this.onClick(mouseX, mouseY);
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.isValidClickButton(button)) {
            this.onRelease(mouseX, mouseY);
            return true;
        } else {
            return false;
        }
    }

    protected boolean isValidClickButton(int button) {
        return button == 0;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isValidClickButton(button)) {
            this.onDrag(mouseX, mouseY, dragX, dragY);
            return true;
        } else {
            return false;
        }
    }

    protected boolean clicked(double mouseX, double mouseY) {
        return this.active && this.visible && mouseX >= (double) this.x && mouseY >= (double) this.y && mouseX < (double) (this.x + this.width) && mouseY < (double) (this.y + this.height);
    }

    public boolean isHovered() {
        return this.isHovered || this.focused;
    }

    public boolean changeFocus(boolean focus) {
        if (this.active && this.visible) {
            this.focused = !this.focused;
            this.onFocusedChanged(this.focused);
            return this.focused;
        } else {
            return false;
        }
    }

    protected void onFocusedChanged(boolean focused) {
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.active && this.visible && mouseX >= (double) this.x && mouseY >= (double) this.y && mouseX < (double) (this.x + this.width) && mouseY < (double) (this.y + this.height);
    }

    public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
    }

    public void playDownSound(SoundHandler handler) {
        handler.play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setMessage(ITextComponent message) {
        if (!Objects.equals(message.getString(), this.message.getString())) {
            this.queueNarration(250);
        }

        this.message = message;
    }

    public void queueNarration(int delay) {
        this.nextNarration = Util.milliTime() + (long) delay;
    }

    public ITextComponent getMessage() {
        return this.message;
    }

    public boolean isFocused() {
        return this.focused;
    }

    protected void setFocused(boolean focused) {
        this.focused = focused;
    }
}


