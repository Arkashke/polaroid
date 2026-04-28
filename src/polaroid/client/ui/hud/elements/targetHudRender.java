package polaroid.client.ui.hud.elements;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.optifine.render.RenderUtils;
import polaroid.client.Polaroid;
import polaroid.client.events.EventDisplay;
import polaroid.client.modules.api.ModuleRegistry;
import polaroid.client.modules.impl.render.InterFace;
import polaroid.client.modules.impl.render.Theme;
import polaroid.client.ui.hud.hudRender;
import polaroid.client.ui.styles.Style;
import polaroid.client.utils.animations.Animation;
import polaroid.client.utils.animations.Direction;
import polaroid.client.utils.animations.impl.EaseBackIn;
import polaroid.client.utils.animations.impl.EaseInOutQuad;
import polaroid.client.utils.client.ClientUtil;
import polaroid.client.utils.drag.Dragging;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.utils.math.StopWatch;
import polaroid.client.utils.math.Vector4i;
import polaroid.client.utils.render.*;
import polaroid.client.utils.render.font.Fonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.opengl.GL11;

import static polaroid.client.utils.shader.ShaderUtil.head;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class targetHudRender implements hudRender {
    final StopWatch stopWatch = new StopWatch();
    final Dragging drag;
    LivingEntity entity = null;
    boolean allow;
    final Animation animation = new EaseInOutQuad(250, 1);
    float healthAnimation = 0.0f;
    float outdatedHealthAnimation = 0.0f;
    float absorptionAnimation = 0.0f;

    @Override
    public void render(EventDisplay eventDisplay) {
        entity = getTarget(entity);

        boolean out = !allow || stopWatch.isReached(1000);
        animation.setDuration(250);
        animation.setDirection(out ? Direction.BACKWARDS : Direction.FORWARDS);

        if (animation.getOutput() == 0.0f) {
            entity = null;
        }

        if (entity != null) {
            float posX = drag.getX();
            float posY = drag.getY();

            float width = 110.0f;
            float height = 32.0f;
            drag.setWidth(width);
            drag.setHeight(height);

            Score score = mc.world.getScoreboard().getOrCreateScore(entity.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));

            float hp = entity.getHealth();
            float maxHp = entity.getMaxHealth();
            float absorption = entity.getAbsorptionAmount();
            String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();

            if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime")
                    && (header.contains("анархия") || header.contains("гриферский")) && entity instanceof PlayerEntity) {
                hp = score.getScorePoints();
                maxHp = 20;
            }

            // Логика для отображения HP и Absorption
            float displayHp = hp;
            float displayAbsorption = 0;
            
            // Добавляем реальный absorption
            if (absorption > 0) {
                displayAbsorption = absorption;
            }
            
            // Если HP больше maxHp, добавляем разницу к absorption
            if (hp > maxHp) {
                displayAbsorption += (hp - maxHp);
                displayHp = maxHp;
            }

            // Анимация для основного HP
            healthAnimation = MathUtil.fast(healthAnimation, MathHelper.clamp(displayHp / maxHp, 0, 1), 10);
            
            if (outdatedHealthAnimation < healthAnimation) {
                outdatedHealthAnimation = healthAnimation;
            } else {
                outdatedHealthAnimation = MathUtil.fast(outdatedHealthAnimation, MathHelper.clamp(displayHp / maxHp, 0, 1), 10);
            }

            // Анимация для absorption
            absorptionAnimation = MathUtil.fast(absorptionAnimation, MathHelper.clamp(displayAbsorption / maxHp, 0, 1), 10);

            float animationValue = (float) animation.getOutput();

            GlStateManager.pushMatrix();
            sizeAnimation(posX + (width / 2), posY + (height / 2), animation.getOutput());

            ModuleRegistry moduleRegistry = Polaroid.getInstance().getFunctionRegistry();
            InterFace interFace = moduleRegistry.getInterFace();
            
            // Получаем цвета в зависимости от темы
            int backgroundColor = interFace.getBackgroundColor();
            int textColor = interFace.getTextColor();
            
            // ОПТИМИЗАЦИЯ: Убрали blur полностью - рисуем только фон
            DisplayUtils.drawRoundedRect(posX, posY, width, height, 6f, backgroundColor);
            
            // Обводка HUD (упрощенная без glow)
            if (interFace.hudOutline.get()) {
                int outlineColor = interFace.getHudOutlineColor(0);
                DisplayUtils.drawRoundedRectOutline(posX, posY, width, height, 6f, 1.2f, outlineColor);
            }

            // Рисуем голову игрока (уменьшен отступ слева)
            float headSize = 26.0f;
            float headPadding = 1.5f; // Уменьшено с 3.0f до 1.5f
            float hurtPercent = (entity.hurtTime - (entity.hurtTime != 0 ? mc.timer.renderPartialTicks : 0.0f)) / 10.0f;
            
            ResourceLocation skin = null;
            if (entity instanceof AbstractClientPlayerEntity) {
                AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) entity;
                NetworkPlayerInfo playerInfo = mc.getConnection().getPlayerInfo(player.getGameProfile().getId());
                if (playerInfo != null) {
                    skin = playerInfo.getLocationSkin();
                }
            }
            
            if (skin == null) {
                String rs = EntityType.getKey(((Entity) entity).getType()).getPath();
                skin = new ResourceLocation("textures/entity/" + rs + ".png");
            }

            drawHead(skin, posX + headPadding, posY + 1.0f, headSize, headSize, 5.0f, animationValue, hurtPercent);

            // Текст справа от головы (уменьшен отступ)
            float textX = posX + headPadding + headSize + 4.0f; // Уменьшено с 6.0f до 4.0f
            float textY = posY + 4.0f;
            
            // Имя игрока
            String name = entity.getName().getString();
            Fonts.otwindowsa.drawText(eventDisplay.getMatrixStack(), name, textX, textY, textColor, 7.0f);

            // HP текст
            float hpTextY = textY + 9.0f;
            String mainHpText = "HP: " + (int) displayHp;
            Fonts.otwindowsa.drawText(eventDisplay.getMatrixStack(), mainHpText, textX, hpTextY, textColor, 6.5f);
            
            // Если есть absorption, рисуем его золотым цветом
            if (displayAbsorption > 0) {
                float mainHpWidth = Fonts.otwindowsa.getWidth(mainHpText, 6.5f);
                String abText = " + " + (int) displayAbsorption + " AB";
                Fonts.otwindowsa.drawText(eventDisplay.getMatrixStack(), abText, textX + mainHpWidth, hpTextY, ColorUtils.rgb(255, 165, 0), 6.5f);
            }

            // Полоса здоровья
            float barX = textX;
            float barY = posY + height - 7.5f;
            float barW = width - (textX - posX) - 4.0f;
            float barH = 4.0f;

            // Фон полосы (темнее основного фона)
            int barBgColor = ColorUtils.rgba(40, 40, 45, (int)(255 * animationValue));
            DisplayUtils.drawRoundedRect(barX, barY, barW, barH, 2.2f, barBgColor);

            // Устаревшая полоса (полупрозрачная тема)
            float outdatedW = barW * outdatedHealthAnimation;
            if (outdatedW > 0.1f) {
                int outdatedBarColor = ColorUtils.setAlpha(Theme.getColor(0), (int)(80 * animationValue));
                DisplayUtils.drawRoundedRect(barX, barY, outdatedW, barH, 2.2f, outdatedBarColor);
            }

            // Основная полоса HP (цвет темы)
            float healthW = barW * healthAnimation;
            if (healthW > 0.1f) {
                int healthBarColor = ColorUtils.setAlpha(Theme.getColor(0), (int)(255 * animationValue));
                DisplayUtils.drawRoundedRect(barX, barY, healthW, barH, 2.2f, healthBarColor);
            }

            // Золотая полоса absorption ПОВЕРХ основной
            if (absorptionAnimation > 0) {
                float absW = barW * absorptionAnimation;
                int goldColor = ColorUtils.setAlpha(ColorUtils.rgb(255, 215, 0), (int)(255 * animationValue));
                DisplayUtils.drawRoundedRect(barX, barY, absW, barH, 2.2f, goldColor);
            }

            GlStateManager.popMatrix();
            
            // Рисуем предметы НАД TargetHud С АНИМАЦИЕЙ масштабирования
            if (entity instanceof PlayerEntity) {
                GlStateManager.pushMatrix();
                sizeAnimation(posX + (width / 2), posY + (height / 2), animation.getOutput());
                drawTargetItems(eventDisplay, (PlayerEntity) entity, posX, posY, animationValue);
                GlStateManager.popMatrix();
            }
        }
    }
    
    // Отрисовка предметов цели (левая рука, броня, правая рука)
    private void drawTargetItems(EventDisplay eventDisplay, PlayerEntity player, float posX, float posY, float alpha) {
        int size = 12;
        int padding = 2;
        java.util.List<net.minecraft.item.ItemStack> items = new java.util.ArrayList<>();
        
        // Добавляем offhand (левая рука)
        net.minecraft.item.ItemStack offStack = player.getHeldItemOffhand();
        if (!offStack.isEmpty()) items.add(offStack);
        
        // Добавляем броню (снизу вверх: ботинки, штаны, нагрудник, шлем)
        for (net.minecraft.item.ItemStack itemStack : player.getArmorInventoryList()) {
            if (!itemStack.isEmpty()) items.add(itemStack);
        }
        
        // Добавляем mainhand (правая рука)
        net.minecraft.item.ItemStack mainStack = player.getHeldItemMainhand();
        if (!mainStack.isEmpty()) items.add(mainStack);
        
        if (items.isEmpty()) return;
        
        // Рисуем предметы слева над TargetHud, ближе к краю
        float startX = posX + 1.5f; // Уменьшено с 3.0f до 1.5f
        float startY = posY - size - 1.0f; // Опускаем ниже, почти на край окна
        
        for (net.minecraft.item.ItemStack itemStack : items) {
            if (itemStack.isEmpty()) continue;
            
            // Рисуем предмет с анимацией прозрачности
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            float scale = size / 16.0f;
            GlStateManager.scaled(scale, scale, scale);
            GlStateManager.color4f(1.0f, 1.0f, 1.0f, alpha);
            mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, (int)(startX / scale), (int)(startY / scale));
            GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
            
            startX += size + padding;
        }
    }
    
    private LivingEntity getTarget(LivingEntity nullTarget) {
        LivingEntity auraTarget = Polaroid.getInstance().getFunctionRegistry().getAura().getTarget();
        LivingEntity target = nullTarget;
        if (auraTarget != null) {
            stopWatch.reset();
            allow = true;
            target = auraTarget;
        } else if (mc.currentScreen instanceof ChatScreen) {
            stopWatch.reset();
            allow = true;
            target = mc.player;
        } else {
            allow = false;
        }
        return target;
    }

    public void drawTargetHead(LivingEntity entity, float x, float y, float width, float height) {
        if (entity != null) {
            EntityRenderer<? super LivingEntity> rendererManager = mc.getRenderManager().getRenderer(entity);
            drawFace(rendererManager.getEntityTexture(entity), x, y, 8F, 8F, 8F, 8F, width, height, 64F, 64F, entity);
        }
    }

    public static void sizeAnimation(double width, double height, double scale) {
        GlStateManager.translated(width, height, 0);
        GlStateManager.scaled(scale, scale, scale);
        GlStateManager.translated(-width, -height, 0);
    }

    public void drawFace(ResourceLocation res, float d,
                         float y,
                         float u,
                         float v,
                         float uWidth,
                         float vHeight,
                         float width,
                         float height,
                         float tileWidth,
                         float tileHeight,
                         LivingEntity target) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        // ИСПРАВЛЕНИЕ: Прямой биндинг текстуры скина
        mc.getTextureManager().bindTexture(res);
        
        float hurtPercent = (target.hurtTime - (target.hurtTime != 0 ? mc.timer.renderPartialTicks : 0.0f)) / 10.0f;
        GL11.glColor4f(1, 1 - hurtPercent, 1 - hurtPercent, 1);
        AbstractGui.drawScaledCustomSizeModalRect(d, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    public static void drawHead(ResourceLocation skin, float x, float y, float width, float height, float radius, float alpha, float hurtPercent) {
        // ИСПРАВЛЕНИЕ: Используем прямой биндинг для скинов игроков
        mc.getTextureManager().bindTexture(skin);
        
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        head.attach();
        head.setUniformf("size", width, height);
        head.setUniformf("radius", radius);
        head.setUniformf("hurt_time", hurtPercent);
        head.setUniformf("alpha", alpha);

        head.setUniformf("startX", 4);
        head.setUniformf("startY", 4);

        head.setUniformf("endX", 8);
        head.setUniformf("endY", 8);

        head.setUniformf("texXSize", 32);
        head.setUniformf("texYSize", 32);
        
        head.drawQuads(x + 2, y + 2, width, height);
        head.detach();
        
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }
}


