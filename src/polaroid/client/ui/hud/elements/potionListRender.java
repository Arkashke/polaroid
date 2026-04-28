package polaroid.client.ui.hud.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import polaroid.client.Polaroid;
import polaroid.client.events.EventDisplay;
import polaroid.client.modules.api.ModuleRegistry;
import polaroid.client.modules.impl.render.InterFace;
import polaroid.client.modules.impl.render.Theme;
import polaroid.client.ui.hud.hudRender;
import polaroid.client.utils.render.*;
import polaroid.client.utils.render.font.Fonts;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class potionListRender implements hudRender {
    
    final List<PotionWindow> potionWindows = new ArrayList<>();
    
    static class PotionWindow {
        EffectInstance effect;
        float x, y;
        float width = 80; // Было 90
        float height = 27; // Было 30
        
        PotionWindow(EffectInstance effect, float x, float y) {
            this.effect = effect;
            this.x = x;
            this.y = y;
        }
    }

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();
        
        List<EffectInstance> activeEffects = new ArrayList<>(mc.player.getActivePotionEffects());
        
        // Если нет активных эффектов, не отображаем ничего
        if (activeEffects.isEmpty()) {
            potionWindows.clear();
            return;
        }

        // Фиксированная позиция: слева посередине экрана
        float baseX = 5; // Отступ от левого края
        int screenHeight = mc.getMainWindow().getScaledHeight();
        int totalEffects = activeEffects.size();
        float spacing = 4; // Было 5
        float windowHeight = 27; // Было 30
        
        // Вычисляем общую высоту всех окон
        float totalHeight = (totalEffects * windowHeight) + ((totalEffects - 1) * spacing);
        
        // Центрируем по вертикали
        float baseY = (screenHeight - totalHeight) / 2f;
        float currentY = baseY;
        
        // Обновляем список окон
        potionWindows.clear();
        for (EffectInstance effect : activeEffects) {
            PotionWindow window = new PotionWindow(effect, baseX, currentY);
            potionWindows.add(window);
            currentY += window.height + spacing;
        }
        
        // Применяем блюр ко всем окнам сразу
        ModuleRegistry moduleRegistry = Polaroid.getInstance().getFunctionRegistry();
        InterFace interFace = moduleRegistry.getInterFace();
        
        // Получаем цвет фона в зависимости от темы
        int backgroundColor = interFace.getBackgroundColor();
        
        // ОПТИМИЗАЦИЯ: Убрали blur полностью - рисуем фоны напрямую
        for (PotionWindow window : potionWindows) {
            DisplayUtils.drawRoundedRect(window.x, window.y, window.width, window.height, 6f, backgroundColor);
        }
        
        // Рендерим каждое окно
        for (PotionWindow window : potionWindows) {
            renderPotionWindow(ms, window);
        }
    }

    private void renderPotionWindow(MatrixStack ms, PotionWindow window) {
        EffectInstance effect = window.effect;
        Effect potionEffect = effect.getPotion();
        
        float x = window.x;
        float y = window.y;
        float width = window.width;
        float height = window.height;
        float padding = 3.5f;
        float fontSize = 7.5f;
        
        // Определяем, является ли эффект негативным
        boolean isBadEffect = !potionEffect.isBeneficial();
        
        // Получаем цвета из InterFace
        ModuleRegistry moduleRegistry = Polaroid.getInstance().getFunctionRegistry();
        InterFace interFace = moduleRegistry.getInterFace();
        
        int backgroundColor = interFace.getBackgroundColor();
        int borderColor = interFace.getBorderColor();
        int textColor = interFace.getTextColor();
        
        // Обводка HUD (упрощенная без glow)
        if (interFace.hudOutline.get()) {
            int outlineColor = interFace.getHudOutlineColor(0);
            DisplayUtils.drawRoundedRectOutline(x, y, width, height, 6f, 1.2f, outlineColor);
        }
        
        // Рисуем иконку эффекта слева
        renderPotionIcon(ms, x + padding, y + height / 2 - 9, potionEffect);
        
        // Получаем информацию об эффекте
        int amp = effect.getAmplifier();
        String ampStr = "";
        if (amp >= 1 && amp <= 9) {
            ampStr = " " + I18n.format("enchantment.level." + (amp + 1));
        }
        String nameText = I18n.format(effect.getEffectName()) + ampStr;
        
        // Получаем время действия
        String durationText = EffectUtils.getPotionDurationString(effect, 1);
        int remainingSeconds = effect.getDuration() / 20;
        
        // Определяем цвет текста из темы
        int nameColor = textColor;
        
        // Цвет таймера с миганием при < 15 секунд
        int timerColor;
        if (remainingSeconds < 15) {
            // Мигание
            float alphaAnim = (float) (Math.sin(System.currentTimeMillis() / 200.0) * 0.5 + 0.5);
            int alphaValue = (int) (alphaAnim * 255);
            timerColor = ColorUtils.setAlpha(textColor, alphaValue);
        } else {
            timerColor = ColorUtils.setAlpha(textColor, 200);
        }
        
        // Рисуем название эффекта (поднято выше и с обрезкой по ширине окна)
        float textX = x + padding + 24;
        float textY = y + padding + 1; // Поднято выше с 3 до 1
        
        // Вычисляем максимальную ширину для текста
        float maxTextWidth = width - (padding + 24 + padding);
        
        // Обрезаем текст если он слишком длинный
        float textWidth = Fonts.otwindowsa.getWidth(nameText, fontSize);
        if (textWidth > maxTextWidth) {
            // Обрезаем текст и добавляем "..."
            while (textWidth > maxTextWidth - Fonts.otwindowsa.getWidth("...", fontSize) && nameText.length() > 0) {
                nameText = nameText.substring(0, nameText.length() - 1);
                textWidth = Fonts.otwindowsa.getWidth(nameText, fontSize);
            }
            nameText += "...";
        }
        
        Fonts.otwindowsa.drawText(ms, nameText, textX, textY, nameColor, fontSize);
        
        // Рисуем таймер под названием (размер чуть-чуть уменьшен)
        float timerY = textY + fontSize + 1;
        
        // Добавляем скобки если включена настройка
        if (interFace.brackets.get()) {
            durationText = "[" + durationText + "]";
        }
        
        Fonts.otwindowsa.drawText(ms, durationText, textX, timerY, timerColor, fontSize - 1.0f); // Уменьшен с -0.5f до -1.0f
    }
    
    private void renderPotionIcon(MatrixStack ms, float x, float y, Effect effect) {
        PotionSpriteUploader potionSpriteUploader = mc.getPotionSpriteUploader();
        TextureAtlasSprite sprite = potionSpriteUploader.getSprite(effect);
        
        // ИСПРАВЛЕНИЕ: Правильный биндинг текстуры для иконок зелий
        mc.getTextureManager().bindTexture(sprite.getAtlasTexture().getTextureLocation());
        
        // Сбрасываем цвет и альфу для правильного отображения
        com.mojang.blaze3d.systems.RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        
        int iconSize = 18;
        
        // Рисуем иконку с правильными UV координатами
        DisplayEffectsScreen.blit(ms, (int)x, (int)y, 0, iconSize, iconSize, sprite);
        
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
    }
}

