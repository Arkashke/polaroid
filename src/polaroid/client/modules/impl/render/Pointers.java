package polaroid.client.modules.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import polaroid.client.Polaroid;
import polaroid.client.command.friends.FriendStorage;
import polaroid.client.events.EventDisplay;
import polaroid.client.modules.api.*;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.utils.math.Vector4i;
import polaroid.client.utils.player.PlayerUtils;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@ModuleSystem(name = "Pointers", type = Category.Render, server = ServerCategory.NO, description = "Треугольники которые показывают где находится игрок")
public class Pointers extends Module {
    public static ModeSetting design = new ModeSetting("Вид стрелки", "Default", "Default", "Celestial", "Nursultan");
    public static SliderSetting distance = new SliderSetting("Дистация от прицела", 60, 15,100,1);
    public float animationStep;
    public Pointers() {
        addSettings(design, distance);
    }
    private float lastYaw;
    private float lastPitch;

    private float animatedYaw;
    private float animatedPitch;

    @Subscribe
    public void onDisplay(EventDisplay e) {
        if (mc.player == null || mc.world == null || e.getType() != EventDisplay.Type.PRE) {
            return;
        }

        ModuleRegistry moduleManager = Polaroid.getInstance().getFunctionRegistry();
        Pointers clientSettings = moduleManager.getPointers();
        float size = clientSettings.distance.get();

        if (mc.currentScreen instanceof InventoryScreen) {
            size += 100;
        }
        animationStep = MathUtil.fast(animationStep, size, 6);
        if (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
            for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
                if (!PlayerUtils.isNameValid(player.getNameClear()) || mc.player == player)
                    continue;

                double x = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * mc.getRenderPartialTicks()
                        - mc.getRenderManager().info.getProjectedView().getX();
                double z = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * mc.getRenderPartialTicks()
                        - mc.getRenderManager().info.getProjectedView().getZ();

                double cos = MathHelper.cos((float) (mc.getRenderManager().info.getYaw() * (Math.PI * 2 / 360)));
                double sin = MathHelper.sin((float) (mc.getRenderManager().info.getYaw() * (Math.PI * 2 / 360)));
                double rotY = -(z * cos - x * sin);
                double rotX = -(x * cos + z * sin);

                float angle = (float) (Math.atan2(rotY, rotX) * 180 / Math.PI);

                double x2 = animationStep * MathHelper.cos((float) Math.toRadians(angle)) + window.getScaledWidth() / 2f;
                double y2 = animationStep * MathHelper.sin((float) Math.toRadians(angle)) + window.getScaledHeight() / 2f;

                x2 += animatedYaw;
                y2 += animatedPitch;

                GlStateManager.pushMatrix();
                GlStateManager.disableBlend();
                GlStateManager.translated(x2, y2, 0);
                GlStateManager.rotatef(angle + 90, 0, 0, 1);

                // Проверяем, является ли игрок другом
                boolean isFriend = FriendStorage.isFriend(player.getGameProfile().getName());

                drawTriangle(isFriend);

                GlStateManager.enableBlend();
                GlStateManager.popMatrix();
            }
        }
        lastYaw = mc.player.rotationYaw;
        lastPitch = mc.player.rotationPitch;
    }

    public static void drawTriangle(boolean isFriend) {
        if(design.is("Default")) {
            // Если друг - используем зеленый цвет, иначе цвет темы
            if (isFriend) {
                int greenColor = ColorUtils.rgb(0, 255, 0);
                DisplayUtils.drawImageAlpha(new ResourceLocation("polaroid/images/modules/pointers/triangle2.png"), -8.0F, -9.0F, 18, 18, new Vector4i(ColorUtils.setAlpha(greenColor, 125), ColorUtils.setAlpha(greenColor, 125), ColorUtils.setAlpha(greenColor, 125), ColorUtils.setAlpha(greenColor, 125)));
            } else {
                DisplayUtils.drawImageAlpha(new ResourceLocation("polaroid/images/modules/pointers/triangle2.png"), -8.0F, -9.0F, 18, 18, new Vector4i(ColorUtils.setAlpha(Theme.getColor(0, 1.0F), 125), ColorUtils.setAlpha(Theme.getColor(90, 1.0F), 125), ColorUtils.setAlpha(Theme.getColor(180, 1.0F), 125), ColorUtils.setAlpha(Theme.getColor(270, 1.0F), 125)));
            }
        }
        if(design.is("Celestial")){
            if (isFriend) {
                int greenColor = ColorUtils.rgb(0, 255, 0);
                DisplayUtils.drawImageAlpha(new ResourceLocation("polaroid/images/modules/pointers/triangle.png"), -8.0F, -9.0F, 18, 18, new Vector4i(ColorUtils.setAlpha(greenColor, 125), ColorUtils.setAlpha(greenColor, 125), ColorUtils.setAlpha(greenColor, 125), ColorUtils.setAlpha(greenColor, 125)));
            } else {
                DisplayUtils.drawImageAlpha(new ResourceLocation("polaroid/images/modules/pointers/triangle.png"), -8.0F, -9.0F, 18, 18, new Vector4i(ColorUtils.setAlpha(Theme.getColor(0, 1.0F), 125), ColorUtils.setAlpha(Theme.getColor(90, 1.0F), 125), ColorUtils.setAlpha(Theme.getColor(180, 1.0F), 125), ColorUtils.setAlpha(Theme.getColor(270, 1.0F), 125)));
            }
        }
        if(design.is("Nursultan")){
            if (isFriend) {
                int greenColor = ColorUtils.rgb(0, 255, 0);
                DisplayUtils.drawImageAlpha(new ResourceLocation("polaroid/images/modules/pointers/arrows.png"), -8.0F, -9.0F, 18, 18, new Vector4i(ColorUtils.setAlpha(greenColor, 125), ColorUtils.setAlpha(greenColor, 125), ColorUtils.setAlpha(greenColor, 125), ColorUtils.setAlpha(greenColor, 125)));
            } else {
                DisplayUtils.drawImageAlpha(new ResourceLocation("polaroid/images/modules/pointers/arrows.png"), -8.0F, -9.0F, 18, 18, new Vector4i(ColorUtils.setAlpha(Theme.getColor(0, 1.0F), 125), ColorUtils.setAlpha(Theme.getColor(90, 1.0F), 125), ColorUtils.setAlpha(Theme.getColor(180, 1.0F), 125), ColorUtils.setAlpha(Theme.getColor(270, 1.0F), 125)));
            }
        }

        GL11.glPushMatrix();
        GL11.glPopMatrix();
    }
}

