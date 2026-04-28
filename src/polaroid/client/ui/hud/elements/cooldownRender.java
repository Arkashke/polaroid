package polaroid.client.ui.hud.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import polaroid.client.Polaroid;
import polaroid.client.events.EventDisplay;
import polaroid.client.modules.api.ModuleRegistry;
import polaroid.client.modules.impl.render.InterFace;
import polaroid.client.modules.impl.render.Theme;
import polaroid.client.ui.hud.hudRender;
import polaroid.client.ui.styles.Style;
import polaroid.client.utils.drag.Dragging;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.utils.math.Vector4i;
import polaroid.client.utils.render.*;
import polaroid.client.utils.render.font.Fonts;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class cooldownRender implements hudRender {

    final Dragging dragging;

    float width;
    float height;

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();
        Minecraft mc = Minecraft.getInstance();

        float posX = dragging.getX();
        float posY = dragging.getY();
        float fontSize = 7;
        float padding = 4;

        GlStateManager.pushMatrix();
        GlStateManager.translated(posX + (width / 2), posY + (height / 2), 0);
        GlStateManager.translated(-(posX + (width / 2)), -(posY + (height / 2)), 0);

        ModuleRegistry moduleRegistry = Polaroid.getInstance().getFunctionRegistry();
        InterFace interFace = moduleRegistry.getInterFace();
        
        // Получаем цвета в зависимости от темы
        int backgroundColor = interFace.getBackgroundColor();
        int borderColor = interFace.getBorderColor();
        int textColor = interFace.getTextColor();
        int separatorColor = interFace.getSeparatorColor();
        
        // ОПТИМИЗАЦИЯ: Убрали blur полностью - рисуем только фон
        DisplayUtils.drawRoundedRect(posX, posY, width, height, 6f, backgroundColor);
        
        // Обводка HUD (упрощенная без glow)
        if (interFace.hudOutline.get()) {
            int outlineColor = interFace.getHudOutlineColor(0);
            DisplayUtils.drawRoundedRectOutline(posX, posY, width, height, 6f, 1.2f, outlineColor);
        }
        Scissor.push();
        Scissor.setFromComponentCoordinates(posX, posY, width, height + 24);

        // Текст слева, иконка справа
        Fonts.otwindowsa.drawText(ms, "Cooldowns", posX + padding, posY + 0 + padding - 0.5f, textColor, 8);
        Fonts.icons2.drawText(ms, "T", posX + width - padding - Fonts.icons2.getWidth("T", 10), posY + 0.25f + padding - 0.5f, textColor, 10, -0.1f);
        posY += fontSize + padding * 2;

        float maxWidth = Fonts.otwindowsa.getWidth("Cooldowns", fontSize, 0.1f) + padding * 2;
        float localHeight = fontSize + padding * 2;

        // Проверяем есть ли активные кулдауны
        boolean hasActiveCooldowns = false;
        for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (!itemStack.isEmpty() && mc.player.getCooldownTracker().hasCooldown(itemStack.getItem())) {
                hasActiveCooldowns = true;
                break;
            }
        }
        
        // Рисуем разделительную линию если есть активные кулдауны (без анимации для этого элемента)
        if (hasActiveCooldowns) {
            DisplayUtils.drawRoundedRect(posX + padding, posY - 2f, width - padding * 2, 1, 
                    new net.minecraft.util.math.vector.Vector4f(0.5f, 0.5f, 0.5f, 0.5f), 
                    separatorColor);
        }

        posY += 3f;

        Set<Item> acitveCooldowns = new HashSet<>();

        for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

//            if (!itemStack.isEmpty() && mc.player.getCooldownTracker().hasCooldown(itemStack.getItem())) {
//                print("Тута кд на айтем: " + itemStack.getItem().getName().getString());
//            }z


            if (!itemStack.isEmpty() && mc.player.getCooldownTracker().hasCooldown(itemStack.getItem())) {
                if (!acitveCooldowns.contains(itemStack.getItem())) {
                    float[] itemWidthHeight = renderCooldownItem(ms, posX, posY, fontSize, padding, itemStack, mc.player.getCooldownTracker().getCooldown(itemStack.getItem(), eventDisplay.getPartialTicks()));
                    float itemWidth = itemWidthHeight[0];
                    float itemHeight = itemWidthHeight[1];
                    
                    if (itemWidth > maxWidth) {
                        maxWidth = itemWidth;
                    }

                    acitveCooldowns.add(itemStack.getItem());
                    posY += itemHeight;
                    localHeight += itemHeight;
                }
            }
        }


        Scissor.unset();
        Scissor.pop();
        GlStateManager.popMatrix();
        
        // Адаптируемся под ширину контента
        width = MathUtil.lerp(width, Math.max(maxWidth, 85.0f), 20.0f);
        height = MathUtil.lerp(height, Math.max(localHeight + 1, 10.0f), 20.0f);
        dragging.setWidth(width);
        dragging.setHeight(height);
    }

    private float[] renderCooldownItem(MatrixStack ms, float posX, float posY, float fontSize, float padding, ItemStack itemStack, float cooldownDuration) {
        float itemHeight = 16; // Уменьшенная высота элемента
        float iconSize = 12; // Уменьшенный размер иконки
        
        // Получаем цвет текста из InterFace
        ModuleRegistry moduleRegistry = Polaroid.getInstance().getFunctionRegistry();
        InterFace interFace = moduleRegistry.getInterFace();
        int textColor = interFace.getTextColor();
        
        // Получаем название предмета
        String itemName = itemStack.getItem().getName().getString();
        if (itemStack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
            itemName = "Чарка";
        } else if (itemStack.getItem() == Items.ENDER_EYE) {
            itemName = "Дезорентация";
        } else if (itemStack.getItem() == Items.NETHERITE_SCRAP) {
            itemName = "Трапка";
        } else if (itemStack.getItem() == Items.SUGAR) {
            itemName = "Явная пыль";
        } else if (itemStack.getItem() == Items.ENDER_PEARL) {
            itemName = "Эндер-жемчуг";
        } else if (itemStack.getItem() == Items.GOLDEN_APPLE) {
            itemName = "Золотое яблоко";
        } else if (itemStack.getItem() == Items.DRIED_KELP) {
            itemName = "Пласт";
        }
        
        // Получаем реальное оставшееся время кулдауна из трекера
        // cooldownDuration - это прогресс от 0 до 1, где 0 = кулдаун закончился, 1 = только начался
        float remainingProgress = cooldownDuration;
        
        // Пытаемся получить реальное время кулдауна через рефлексию
        int maxCooldownTicks = 100; // По умолчанию 5 секунд (100 тиков)
        
        try {
            // Получаем доступ к приватному полю cooldowns в CooldownTracker
            java.lang.reflect.Field cooldownsField = mc.player.getCooldownTracker().getClass().getDeclaredField("cooldowns");
            cooldownsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Item, ?> cooldowns = (Map<Item, ?>) cooldownsField.get(mc.player.getCooldownTracker());
            
            Object cooldown = cooldowns.get(itemStack.getItem());
            if (cooldown != null) {
                // Получаем createTicks и expireTicks
                java.lang.reflect.Field createTicksField = cooldown.getClass().getDeclaredField("createTicks");
                java.lang.reflect.Field expireTicksField = cooldown.getClass().getDeclaredField("expireTicks");
                createTicksField.setAccessible(true);
                expireTicksField.setAccessible(true);
                
                int createTicks = createTicksField.getInt(cooldown);
                int expireTicks = expireTicksField.getInt(cooldown);
                maxCooldownTicks = expireTicks - createTicks;
            }
        } catch (Exception e) {
            // Если рефлексия не сработала, используем стандартные значения
            if (itemStack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
                maxCooldownTicks = 1200; // 60 секунд для чарки
            } else if (itemStack.getItem() == Items.GOLDEN_APPLE) {
                maxCooldownTicks = 100; // 5 секунд для золотого яблока
            } else if (itemStack.getItem() == Items.ENDER_PEARL) {
                maxCooldownTicks = 20; // 1 секунда для эндер-жемчуга
            } else if (itemStack.getItem() == Items.CHORUS_FRUIT) {
                maxCooldownTicks = 20; // 1 секунда для хорус фрукта
            } else if (itemStack.getItem() == Items.SHIELD) {
                maxCooldownTicks = 100; // 5 секунд для щита
            }
        }
        
        // Вычисляем оставшееся время в секундах
        float remainingSeconds = (maxCooldownTicks * remainingProgress) / 20.0f;
        
        // Форматируем время
        String timeText;
        if (remainingSeconds >= 10.0f) {
            timeText = String.format("%.0fs", remainingSeconds); // Без десятичных для больших значений
        } else if (remainingSeconds >= 1.0f) {
            timeText = String.format("%.1fs", remainingSeconds);
        } else {
            timeText = String.format("%.1fs", remainingSeconds);
        }
        
        // Добавляем скобки если настройка включена
        if (interFace.brackets.get()) {
            timeText = "[" + timeText + "]";
        }
        
        // Вычисляем ширину элемента
        float nameWidth = Fonts.otwindowsa.getWidth(itemName, fontSize);
        float timeWidth = Fonts.otwindowsa.getWidth(timeText, fontSize - 0.5f);
        float itemWidth = iconSize + 4 + nameWidth + timeWidth + padding * 3; // iconSize + отступ
        
        // Рисуем иконку предмета слева (уменьшенная)
        GlStateManager.pushMatrix();
        float scale = iconSize / 16.0f; // Масштаб для уменьшения иконки
        GlStateManager.scaled(scale, scale, scale);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, (int)((posX + padding) / scale), (int)((posY) / scale));
        GlStateManager.popMatrix();
        
        // Рисуем название предмета (цвет зависит от темы)
        float textX = posX + padding + iconSize + 4;
        float textY = posY + 1;
        Fonts.otwindowsa.drawText(ms, itemName, textX, textY, textColor, fontSize);
        
        // Рисуем таймер справа (цвет зависит от темы)
        float timerX = posX + width - padding - Fonts.otwindowsa.getWidth(timeText, fontSize - 0.5f);
        float timerY = posY + 1;
        
        // Цвет таймера с миганием при < 3 секунд
        int timerColor;
        if (remainingSeconds < 3.0f) {
            // Мигание
            float alpha = (float) (Math.sin(System.currentTimeMillis() / 200.0) * 0.5 + 0.5);
            int alphaValue = (int) (alpha * 255);
            timerColor = ColorUtils.setAlpha(textColor, alphaValue);
        } else {
            timerColor = textColor;
        }
        
        Fonts.otwindowsa.drawText(ms, timeText, timerX, timerY, timerColor, fontSize - 0.5f);
        
        return new float[]{itemWidth, itemHeight + 3}; // Возвращаем ширину и высоту элемента
    }
}


