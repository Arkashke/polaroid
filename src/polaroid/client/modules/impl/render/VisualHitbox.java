package polaroid.client.modules.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import polaroid.client.command.friends.FriendStorage;
import polaroid.client.events.WorldEvent;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.impl.combat.AntiBot;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ModeListSetting;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.utils.render.ColorUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

@ModuleSystem(name = "VisualHitbox", type = Category.Render, server = ServerCategory.NO, description = "Отображает хитбоксы энтити")
public class VisualHitbox extends Module {

    public ModeListSetting targets = new ModeListSetting("Отображать",
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Мобы", false),
            new BooleanSetting("Предметы", false)
    );

    public VisualHitbox() {
        addSettings(targets);
    }

    @Subscribe
    public void onRender(WorldEvent e) {
        if (mc.world == null) {
            return;
        }

        Vector3d cam = mc.getRenderManager().info.getProjectedView();
        float partialTicks = e.getPartialTicks();

        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();

        for (Entity entity : mc.world.getAllEntities()) {
            if (!isValid(entity)) continue;

            boolean isPlayer = entity instanceof PlayerEntity && entity != mc.player;
            boolean isMob = entity instanceof LivingEntity && !(entity instanceof PlayerEntity);
            boolean isItem = entity instanceof ItemEntity;

            if (!(isPlayer && targets.getValueByName("Игроки").get()
                    || isMob && targets.getValueByName("Мобы").get()
                    || isItem && targets.getValueByName("Предметы").get())) {
                continue;
            }

            // Интерполяция позиции для плавного движения
            double x = MathUtil.interpolate(entity.getPosX(), entity.lastTickPosX, partialTicks);
            double y = MathUtil.interpolate(entity.getPosY(), entity.lastTickPosY, partialTicks);
            double z = MathUtil.interpolate(entity.getPosZ(), entity.lastTickPosZ, partialTicks);

            // Получаем размер хитбокса
            AxisAlignedBB entityBox = entity.getBoundingBox();
            double width = entityBox.maxX - entityBox.minX;
            double height = entityBox.maxY - entityBox.minY;
            double depth = entityBox.maxZ - entityBox.minZ;

            // Создаем хитбокс с интерполированной позицией
            AxisAlignedBB box = new AxisAlignedBB(
                    x - width / 2, y, z - depth / 2,
                    x + width / 2, y + height, z + depth / 2
            ).offset(-cam.x, -cam.y, -cam.z);

            int color;
            if (entity instanceof PlayerEntity && FriendStorage.isFriend(entity.getName().getString())) {
                color = ColorUtils.rgb(0, 255, 0); // Яркий зеленый для друзей
            } else {
                color = Theme.getColor(0);
            }

            int fillColor = ColorUtils.setAlpha(color, 50);
            int outlineColor = ColorUtils.setAlpha(color, 150);

            drawFilledBox(box, fillColor);
            drawOutlineBox(box, outlineColor);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    private void drawFilledBox(AxisAlignedBB box, int color) {
        float[] rgba = ColorUtils.rgba(color);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        // Нижняя грань
        buffer.pos(box.minX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

        // Верхняя грань
        buffer.pos(box.minX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

        // Передняя грань
        buffer.pos(box.minX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

        // Задняя грань
        buffer.pos(box.minX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

        // Левая грань
        buffer.pos(box.minX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

        // Правая грань
        buffer.pos(box.maxX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

        Tessellator.getInstance().draw();
    }

    private void drawOutlineBox(AxisAlignedBB box, int color) {
        float[] rgba = ColorUtils.rgba(color);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        GL11.glLineWidth(2.0f);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        // Нижние линии
        buffer.pos(box.minX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

        buffer.pos(box.maxX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

        buffer.pos(box.maxX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

        buffer.pos(box.minX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

        // Верхние линии
        buffer.pos(box.minX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

        buffer.pos(box.maxX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

        buffer.pos(box.maxX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

        buffer.pos(box.minX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

        // Вертикальные линии
        buffer.pos(box.minX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

        buffer.pos(box.maxX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

        buffer.pos(box.maxX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

        buffer.pos(box.minX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

        Tessellator.getInstance().draw();
    }

    private boolean isValid(Entity entity) {
        if (entity == mc.player) return false;
        if (AntiBot.isBot(entity)) return false;
        if (!entity.isAlive()) return false;
        
        return true;
    }
}


