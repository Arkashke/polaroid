package polaroid.client.ui.clickgui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.ui.clickgui.objects.ModuleObject;
import polaroid.client.ui.clickgui.objects.Object;
import polaroid.client.Polaroid;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.impl.render.Theme;
import polaroid.client.modules.impl.render.InterFace;
import polaroid.client.ui.styles.Style;
import polaroid.client.ui.styles.StyleManager;
import polaroid.client.utils.render.*;
import polaroid.client.utils.render.font.Fonts;
import polaroid.client.utils.math.MathUtil;

import java.awt.*;
import java.util.ArrayList;

import static polaroid.client.utils.client.IMinecraft.mc;

public class Panel {
    Category category;
    float x;
    float y;
    float width;
    float height;
    float scrolling;
    float scrollingOut;
    float animationProgress;
    ArrayList<ModuleObject> moduleObjects = new ArrayList<>();
    private boolean isOpen;

    public Panel(Category category, float x, float y, float width, float height) {
        this.category = category;
        this.x = x + -20;
        this.y = y + 19f;
        this.width = width - 5;
        this.height = height - 75f;
        this.animationProgress = 0.0f;
        this.isOpen = false;
        for (Module m2 : Polaroid.getInstance().getFunctionRegistry().getFunctions()) {
            if (m2.getCategory() == category) {
                this.moduleObjects.add(new ModuleObject(m2));
            }
        }
    }

    int firstColor = Theme.getColor(0);
    int secondColor = Theme.getColor(90);

    public static String hoveredDescription = "";


    public void render(MatrixStack matrixStack, int mouseX, int mouseY) {
        MatrixStack ms = new MatrixStack();
        this.scrollingOut = MathUtil.fast(this.scrollingOut, this.scrolling, 15.0f);
        int activColore = Polaroid.getInstance().getStyleManager().getCurrentStyle().getFirstColor().getRGB();
        int alpha = ColorUtils.setAlpha(activColore, 255);

        float centereX = this.x + this.width / 2f;
        float iconY = y - 20f;
        String iconChar = this.category.getIcon();

        // ОПТИМИЗАЦИЯ: Кэшируем blur для панели (каждый 5-й кадр вместо 3-го)
        // Фон панели (блюр удален, оптимальная прозрачность)
        DisplayUtils.drawRoundedRect(this.x + 5, y, this.width + 8.6f, this.height + 35, 
            new Vector4f(7.4f, 7.4f, 7.4f, 7.4f), ColorUtils.rgba(16, 16, 16, 185));
        
        DisplayUtils.drawRoundedRect(this.x + 5, y, this.width + 8.6f, this.height + 35, 
            new Vector4f(7.4f, 7.4f, 7.4f, 7.4f), ColorUtils.rgba(16, 16, 16, 150));
        
        // ОПТИМИЗАЦИЯ: Кэшируем ширину текста
        float iconWidth = polaroid.client.utils.performance.FontWidthCache.getInstance()
            .getWidth(iconChar, 10, (text, size) -> Fonts.excellenticon.getWidth(text, size));
        float nameWidth = polaroid.client.utils.performance.FontWidthCache.getInstance()
            .getWidth(this.category.getName(), 8, (text, size) -> Fonts.otwindowsa.getWidth(text, size));
        
        Fonts.excellenticon.drawText(ms, iconChar, this.x + 12, y + 8, alpha, 10);
        Fonts.otwindowsa.drawText(ms, this.category.getName(), this.x + 25, y + 8f, 
            ColorUtils.rgba(255, 255, 255, 255), 8);

        float originalWidth = this.width - 1.0f;
        float originalHeight = 15.0f;
        float off = 0f;

        // ОПТИМИЗАЦИЯ: Используем оптимизированный scissor
        polaroid.client.utils.performance.ScissorOptimizer.getInstance()
            .setScissor(this.x + 5, y + 25, this.width + 10, this.height + 10);

        hoveredDescription = "";

        for (ModuleObject m : moduleObjects) {
            m.width = originalWidth;
            m.height = originalHeight;
            m.x = this.x + 1.0f;
            m.y = y + 30f + off + this.scrollingOut;
            
            // ОПТИМИЗАЦИЯ: Не рендерим модули вне видимой области
            if (m.y + m.height < y + 25 || m.y > y + this.height + 35) {
                // Пропускаем невидимые модули, но учитываем их высоту
                float totalHeight = 0;
                for (Object object1 : m.object) {
                    if (object1.setting != null && object1.setting.visible != null) {
                        try {
                            Boolean visibleValue = (Boolean) object1.setting.visible.get();
                            if (Boolean.TRUE.equals(visibleValue)) {
                                totalHeight += 12f;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
                
                float moduleHeight = m.module.expanded ?
                        m.height + (totalHeight * m.expand_anim) :
                        m.height;
                off += moduleHeight + 3f;
                continue;
            }

            float totalHeight = 0;
            for (Object object1 : m.object) {
                if (object1.setting != null && object1.setting.visible != null) {
                    try {
                        Boolean visibleValue = (Boolean) object1.setting.visible.get();
                        if (Boolean.TRUE.equals(visibleValue)) {
                            totalHeight += 12f; // Fixed height per setting
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            float moduleHeight = m.module.expanded ?
                    m.height + (totalHeight * m.expand_anim) :
                    m.height;

            int activColor = Polaroid.getInstance().getStyleManager().getCurrentStyle().getFirstColor().getRGB();

            if (m.module.isState()) {
                DisplayUtils.drawRoundedRect(m.x + 7.5f, m.y, m.width + 3,
                        moduleHeight + 2.5f,
                        new Vector4f(3.5f, 3.5f, 3.5f, 3.5f),
                        ColorUtils.setAlpha(activColor, 120));

                DisplayUtils.drawRoundedRectOutline(m.x + 7.5f, m.y, m.width + 3,
                        moduleHeight + 2.5f,
                        3.5f, 1.0f,
                        ColorUtils.setAlpha(activColor, 200));
            } else {
                DisplayUtils.drawRoundedRect(m.x + 7.5f, m.y, m.width + 3,
                        moduleHeight + 2.5f,
                        new Vector4f(3.5f, 3.5f, 3.5f, 3.5f),
                        ColorUtils.rgba(58, 58, 58, 50));

                DisplayUtils.drawRoundedRectOutline(m.x + 7.5f, m.y, m.width + 3,
                        moduleHeight + 2.5f,
                        3.5f, 1.0f,
                        ColorUtils.rgba(48, 48, 48, 128));
            }

            String namem = m.isBinding ? "[...]" : m.module.getName();
            Fonts.otwindowsa.drawText(ms, namem, this.x + 13.0f,
                    m.y + 5f,
                    m.module.isState() ? ColorUtils.rgba(255, 255, 255, 255) :
                            ColorUtils.setAlpha(new Color(180, 180, 180, 255).getRGB(), 255), 8);

            m.expand_anim = MathUtil.fast(m.expand_anim, m.module.expanded ? 1.0f : 0.0f, 10.0f);
            if (!m.module.getSettings().isEmpty()) {
                Fonts.excellenticon.drawText(ms, "f", m.x + m.width + 3, m.y + 5.5f,
                        m.module.isState() ? ColorUtils.setAlpha(activColor, 255) :
                                ColorUtils.setAlpha(new Color(140, 140, 140, 128).getRGB(), 255), 8);
            }

            boolean hovered = mouseX >= m.x && mouseX <= m.x + m.width
                    && mouseY >= m.y && mouseY <= m.y + m.height;

            if (hovered) {
                hoveredDescription = m.module.getDescription();
            }


            float settingsOffset = 0f;
            for (Object object1 : m.object) {
                if (object1.setting == null || object1.setting.visible == null) continue;
                
                try {
                    Boolean visibleValue = (Boolean) object1.setting.visible.get();
                    if (!Boolean.TRUE.equals(visibleValue)) continue;
                } catch (Exception e) {
                    continue;
                }
                
                object1.x = this.x + 10;
                object1.y = m.y + m.height + 3f + settingsOffset;
                object1.width = this.width - 5;
                object1.height = 10.0f;
                
                if (m.expand_anim > 0.1) {
                    object1.draw(ms, mouseX, mouseY);
                }
                settingsOffset += 12f * m.expand_anim; // Fixed spacing
            }
            off += moduleHeight + 3f;
        }

        // ОПТИМИЗАЦИЯ: Отключаем scissor через оптимизатор
        polaroid.client.utils.performance.ScissorOptimizer.getInstance().disable();

        if (!hoveredDescription.isEmpty()) {
            String desc = hoveredDescription;
            float centerX1 = mc.getMainWindow().getScaledWidth() / 2f;
            float textWidth = Fonts.otwindowsa.getWidth(desc, 10);
            float textXs = centerX1 - textWidth / 2f;
            float textYs = y - 40;

            // Тень для текста
            Fonts.otwindowsa.drawText(ms, desc, textXs + 1, textYs, ColorUtils.rgba(0, 0, 0, 180), 10);
            Fonts.otwindowsa.drawText(ms, desc, textXs - 1, textYs, ColorUtils.rgba(0, 0, 0, 180), 10);
            Fonts.otwindowsa.drawText(ms, desc, textXs, textYs + 1, ColorUtils.rgba(0, 0, 0, 180), 10);
            Fonts.otwindowsa.drawText(ms, desc, textXs, textYs - 1, ColorUtils.rgba(0, 0, 0, 180), 10);

            Fonts.otwindowsa.drawText(ms, desc, textXs, textYs, ColorUtils.rgba(255, 255, 255, 255), 10);
        }


        float maxHeight = off;
        this.scrolling = maxHeight < this.height ? 0.0f : MathHelper.clamp(this.scrolling, -(maxHeight - this.height + 20), 0.0f);
    }

    public void onClick(double mouseX, double mouseY, int button) {
        int zoneX = (int) this.x;
        int zoneY = (int) this.y + 18;
        int zoneWidth = (int) this.width;
        int zoneHeight = (int) this.height + 12;

        if (MathUtil.isHovered((float)mouseX, (float)mouseY, zoneX, zoneY, zoneWidth, zoneHeight)) {
            for (ModuleObject m : this.moduleObjects) {
                m.mouseClicked((int) mouseX, (int) mouseY, button);

                if (MathUtil.isHovered((float)mouseX, (float)mouseY, m.x + 8, m.y, m.width - 16, m.height) && button == 1) {
                    m.module.expanded = !m.module.expanded;
                    isOpen = m.module.expanded;
                }
            }
        }
    }

    public void onScroll(double mouseX, double mouseY, double delta) {
        if (MathUtil.isHovered((float)mouseX, (float)mouseY, this.x, this.y, this.width, this.height + 32)) {
            this.scrolling += (float) (delta * 25.0);
        }
    }

    public void onRelease(double mouseX, double mouseY, int button) {
        for (ModuleObject m : this.moduleObjects) {
            for (Object o : m.object) {
                o.mouseReleased((int) mouseX, (int) mouseY, button);
            }
        }
    }

    public void onKey(int keyCode, int scanCode, int modifiers) {
        for (ModuleObject m : this.moduleObjects) {
            m.keyTyped(keyCode, scanCode, modifiers);
        }
    }
}


