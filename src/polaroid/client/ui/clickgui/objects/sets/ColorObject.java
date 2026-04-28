package polaroid.client.ui.clickgui.objects.sets;

import com.mojang.blaze3d.matrix.MatrixStack;
import polaroid.client.modules.settings.impl.ColorSetting;
import polaroid.client.ui.clickgui.objects.ModuleObject;
import polaroid.client.ui.clickgui.objects.Object;
import polaroid.client.utils.render.font.Fonts;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;

import java.awt.*;

public class ColorObject extends Object {
    public ModuleObject object;
    public ColorSetting setting;
    private float hue;
    private float saturation;
    private float brightness;
    private float alpha;
    private boolean colorSelectorDragging;
    private boolean hueSelectorDragging;
    private boolean alphaSelectorDragging;

    public ColorObject(ModuleObject object, ColorSetting setting) {
        height = 11;
        this.object = object;
        this.setting = setting;
        float[] hsb = RGBtoHSB(setting.get());
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];

        this.alpha = (setting.get() >> 24 & 0xFF) / 255F;
    }

    protected boolean extended;
    
    @Override
    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        height = (extended ? 88 : 5.5F);
        Fonts.otwindowsa.drawText(stack, setting.getName(), x + 12.5f, y + 2.4f, -1, 6);
        DisplayUtils.drawRoundedRect(x + width - 21, y - 1, 9, 9, 2.5F, setting.get());
        DisplayUtils.drawRoundedRectOutline(x + width - 21, y - 1, 9, 9, 2.5F, 0.5f, new Color(73, 73, 73, 128).getRGB());

        if (extended) {
            if (colorSelectorDragging && isInRegion(mouseX, mouseY, x + 11.5f, y + 12, 90 - 15 - 2, 60)) {
                float xDifference = mouseX - (x + 5);
                saturation = xDifference / 90;
                float yDifference = mouseY - (y + 12);
                brightness = 1 - yDifference / 60;
                setting.set(ColorUtils.setAlpha(new Color(Color.HSBtoRGB(hue, saturation, brightness)).getRGB(), (int)(alpha * 255)));
            }

            DisplayUtils.drawGradientRound(x + 11.5f, y + 12, 90 - 15 - 2, 60, 1, -1, Color.BLACK.getRGB(), Color.HSBtoRGB(hue, 1, 1), Color.BLACK.getRGB());
            float selectorX = x + 5 + saturation * (90 - 3);
            float selectorY = y + 10 + (1 - brightness) * 60;

            DisplayUtils.drawRoundedRect(selectorX, selectorY, 4, 4, 2, -1);
            DisplayUtils.drawRoundedRectOutline(selectorX, selectorY, 4, 4, 2, 0.5f, new Color(78, 76, 76, 128).getRGB());

            if (hueSelectorDragging && isInRegion(mouseX, mouseY, x + 5, y, 90, extended ? 93 : 11)) {
                float xDifference = mouseX - (x + 5);
                hue = xDifference / 90;
                setting.set(ColorUtils.setAlpha(new Color(Color.HSBtoRGB(hue, saturation, brightness)).getRGB(), (int)(alpha * 255)));
            }

            float times = 5;
            float size = 91 / times;
            float sliderX = x + 5;
            float sliderY = y + 77;

            float hueSelectorX = hue * 86;

            for (int i = 0; i < times; i++) {
                boolean last = i == 4;
                if (last) {
                    size--;
                }
                int color1 = Color.HSBtoRGB(0.2F * i, 1, 1);
                int color2 = Color.HSBtoRGB(0.2F * (i + 1), 1, 1);

                if (i == 0) {
                    DisplayUtils.drawGradientRound(sliderX, sliderY, size, 5, 4, color1, color1, color2, color2);
                } else if (last) {
                    DisplayUtils.drawGradientRound(sliderX, sliderY, size, 5, 4, color1, color1, color2, color2);
                } else {
                    DisplayUtils.drawGradientRound(sliderX, sliderY, size, 5, 0, color1, color1, color2, color2);
                }

                if (!last) {
                    sliderX += size;
                }
            }

            DisplayUtils.drawRoundedRect(x + 5 + hueSelectorX, sliderY, 5, 5, 2, -1);

            int color = Color.HSBtoRGB(hue, saturation, brightness);

            if (alphaSelectorDragging && isInRegion(mouseX, mouseY, x + 5, y, 90, extended ? 93 : 11)) {
                float xDifference = mouseX - (x + 5);
                alpha = xDifference / 90;
                setting.set(ColorUtils.setAlpha(color, (int)(alpha * 255)));
            }

            DisplayUtils.drawGradientRound(x + 5, y + 85, 90, 5, 2, -1, -1, color, color);

            float alphaSelectorX = alpha * 86;
            DisplayUtils.drawRoundedRect(x + 5 + alphaSelectorX, y + 85, 5, 5, 2, -1);
        }
    }
    
    private boolean isInRegion(int mouseX, int mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isInRegion(mouseX, mouseY, x, y, width, 11) && mouseButton == 1) {
            extended = !extended;
        }

        if (!colorSelectorDragging && isInRegion(mouseX, mouseY, x + 5, y + 12, 90, 60) && mouseButton == 0) {
            colorSelectorDragging = true;
        }

        if (!hueSelectorDragging && isInRegion(mouseX, mouseY, x + 5, y + 77, 90, 5) && mouseButton == 0) {
            hueSelectorDragging = true;
        }

        if (!alphaSelectorDragging && isInRegion(mouseX, mouseY, x + 5, y + 85, 90, 5) && mouseButton == 0) {
            alphaSelectorDragging = true;
        }
    }

    private float[] RGBtoHSB(int color) {
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        return Color.RGBtoHSB(red, green, blue, null);
    }

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        colorSelectorDragging = false;
        hueSelectorDragging = false;
        alphaSelectorDragging = false;
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
    }
}


