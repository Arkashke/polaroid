package polaroid.client.modules.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import polaroid.client.Polaroid;
import polaroid.client.command.friends.FriendStorage;
import polaroid.client.events.EventDisplay;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.impl.render.Theme;
import polaroid.client.modules.impl.combat.AntiBot;
import polaroid.client.modules.impl.misc.NameProtect;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ColorSetting;
import polaroid.client.modules.settings.impl.ModeListSetting;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.utils.math.Vector4i;
import polaroid.client.utils.projections.ProjectionUtil;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.font.Fonts;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.*;
import org.lwjgl.opengl.GL11;

import java.util.*;

import static net.minecraft.client.renderer.WorldRenderer.frustum;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

@ModuleSystem(name = "EntityESP", type = Category.Render, server = ServerCategory.NO, description = "Позволяет видеть игроков через стены")
public class ESP extends Module {
    public ModeListSetting remove = new ModeListSetting("Убрать", new BooleanSetting("Боксы", false), new BooleanSetting("Полоску хп", false), new BooleanSetting("Зачарования", false), new BooleanSetting("Список эффектов", false));
    public ModeListSetting targets = new ModeListSetting("Отображать",
            new BooleanSetting("Себя", true),
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Предметы", false),
            new BooleanSetting("Мобы", false)
    );
    
    float healthAnimation = 0.0f;

    public ESP() {
        addSettings(targets, remove);
    }

    float length;

    private final HashMap<Entity, Vector4f> positions = new HashMap<>();

    public ColorSetting color = new ColorSetting("Color", -1);

    @Subscribe
    public void onDisplay(EventDisplay e) {
        if (mc.world == null || e.getType() != EventDisplay.Type.PRE) {
            return;
        }

        positions.clear();

        Vector4i colors = new Vector4i(Theme.getColor(0), Theme.getColor(0), Theme.getColor(0), Theme.getColor(0));
        Vector4i friendColors = new Vector4i(Theme.getColor(ColorUtils.rgb(144, 238, 144), ColorUtils.rgb(0, 139, 0), 0, 1), Theme.getColor(ColorUtils.rgb(144, 238, 144), ColorUtils.rgb(0, 139, 0), 90, 1), Theme.getColor(ColorUtils.rgb(144, 238, 144), ColorUtils.rgb(0, 139, 0), 180, 1), Theme.getColor(ColorUtils.rgb(144, 238, 144), ColorUtils.rgb(0, 139, 0), 270, 1));

        for (Entity entity : mc.world.getAllEntities()) {
            if (!isValid(entity)) continue;
            if (!(entity instanceof PlayerEntity && entity != mc.player && targets.getValueByName("Игроки").get()
                    || entity instanceof ItemEntity && targets.getValueByName("Предметы").get()
                    || (entity instanceof AnimalEntity || entity instanceof MobEntity) && targets.getValueByName("Мобы").get()
                    || entity == mc.player && targets.getValueByName("Себя").get() && !(mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON))
            ) continue;

            double x = MathUtil.interpolate(entity.getPosX(), entity.lastTickPosX, e.getPartialTicks());
            double y = MathUtil.interpolate(entity.getPosY(), entity.lastTickPosY, e.getPartialTicks());
            double z = MathUtil.interpolate(entity.getPosZ(), entity.lastTickPosZ, e.getPartialTicks());

            Vector3d size = new Vector3d(entity.getBoundingBox().maxX - entity.getBoundingBox().minX, entity.getBoundingBox().maxY - entity.getBoundingBox().minY, entity.getBoundingBox().maxZ - entity.getBoundingBox().minZ);

            AxisAlignedBB aabb = new AxisAlignedBB(x - size.x / 2f, y, z - size.z / 2f, x + size.x / 2f, y + size.y, z + size.z / 2f);

            Vector4f position = null;
            for (int i = 0; i < 8; i++) {
                Vector2f vector = ProjectionUtil.project(i % 2 == 0 ? aabb.minX : aabb.maxX, (i / 2) % 2 == 0 ? aabb.minY : aabb.maxY, (i / 4) % 2 == 0 ? aabb.minZ : aabb.maxZ);

                if (position == null) {
                    position = new Vector4f(vector.x, vector.y, 1, 1.0f);
                } else {
                    position.x = Math.min(vector.x, position.x);
                    position.y = Math.min(vector.y, position.y);
                    position.z = Math.max(vector.x, position.z);
                    position.w = Math.max(vector.y, position.w);
                }
            }

            positions.put(entity, position);
        }
        
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);

        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (Map.Entry<Entity, Vector4f> entry : positions.entrySet()) {
            Vector4f position = entry.getValue();
            if (entry.getKey() instanceof LivingEntity entity) {
                if (!remove.getValueByName("Боксы").get()) {
                    DisplayUtils.drawBox(position.x - 0.5f, position.y - 0.5f, position.z + 0.5f, position.w + 0.5f, 2, ColorUtils.rgba(0, 0, 0, 128));
                    DisplayUtils.drawBoxTest(position.x, position.y, position.z, position.w, 1, FriendStorage.isFriend(entity.getName().getString()) ? friendColors : colors);
                }
                float hpOffset = 3f;
                float out = 0.5f;
                if (!remove.getValueByName("Полоску хп").get()) {
                    String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();

                    DisplayUtils.drawRectBuilding(position.x - hpOffset - out, position.y - out, position.x - hpOffset + 1 + out, position.w + out, ColorUtils.rgba(0, 0, 0, 128));
                    DisplayUtils.drawRectBuilding(position.x - hpOffset, position.y, position.x - hpOffset + 1, position.w, ColorUtils.rgba(0, 0, 0, 128));
                    
                    Score score = mc.world.getScoreboard().getOrCreateScore(entity.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));

                    float hp = entity.getHealth();
                    float maxHp = entity.getMaxHealth();

                    if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime") && (header.contains("анархия") || header.contains("гриферский"))) {
                        hp = score.getScorePoints();
                        maxHp = 20;
                    }

                    DisplayUtils.drawMCVerticalBuilding(position.x - hpOffset, position.y + (position.w - position.y) * (1 - MathHelper.clamp(hp / maxHp, 0, 1)), position.x - hpOffset + 1, position.w, FriendStorage.isFriend(entity.getName().getString()) ? friendColors.w : colors.w, FriendStorage.isFriend(entity.getName().getString()) ? friendColors.x : colors.x);
                }
            }
        }
        Tessellator.getInstance().draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();

        for (Map.Entry<Entity, Vector4f> entry : positions.entrySet()) {
            Entity entity = entry.getKey();

            if (entity instanceof LivingEntity living) {
                Score score = mc.world.getScoreboard().getOrCreateScore(living.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));
                float hp = living.getHealth();
                float maxHp = living.getMaxHealth();

                String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();
                
                if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime") && (header.contains("анархия") || header.contains("гриферский"))) {
                    hp = score.getScorePoints();
                    maxHp = 20;
                }

                Vector4f position = entry.getValue();
                float width = position.z - position.x;

                healthAnimation = MathUtil.fast(healthAnimation, MathHelper.clamp(hp / maxHp, 0, 1), 10);

                // НЕЙМТЕГИ УДАЛЕНЫ - используй отдельный модуль NameTags

                if (!remove.getValueByName("Список эффектов").get()) {
                    drawPotions(e.getMatrixStack(), living, position.z + 2, position.y);
                }
                drawItems(e.getMatrixStack(), living, (int) (position.x + width / 2f), (int) (position.y - 14.5f));
            } else if (entity instanceof ItemEntity item) {
                Vector4f position = entry.getValue();
                float width = position.z - position.x;
                float length = mc.fontRenderer.getStringPropertyWidth(entity.getDisplayName());
                GL11.glPushMatrix();

                glCenteredScale(position.x + width / 2f - length / 2f, position.y - 7, length, 10, 0.5f);

                mc.fontRenderer.func_243246_a(e.getMatrixStack(), entity.getDisplayName(), position.x + width / 2f - length / 2f, position.y - 7, -1);
                GL11.glPopMatrix();
            }
        }
    }

    public boolean isInView(Entity ent) {

        if (mc.getRenderViewEntity() == null) {
            return false;
        }
        frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x, mc.getRenderManager().info.getProjectedView().y, mc.getRenderManager().info.getProjectedView().z);
        return frustum.isBoundingBoxInFrustum(ent.getBoundingBox()) || ent.ignoreFrustumCheck;
    }
    int index = 0;
    private void drawPotions(MatrixStack matrixStack, LivingEntity entity, float posX, float posY) {
        for (Iterator var8 = entity.getActivePotionEffects().iterator(); var8.hasNext(); ++index) {
            EffectInstance effectInstance = (EffectInstance)var8.next();

            int amp = effectInstance.getAmplifier() + 1;
            String ampStr = "";

            if (amp >= 1 && amp <= 9) {
                ampStr = " " + I18n.format("enchantment.level." + (amp + 1));
            }

            String text = I18n.format(effectInstance.getEffectName(), new Object[0]) + ampStr + " - " + EffectUtils.getPotionDurationString(effectInstance, 1);
            PotionSpriteUploader potionspriteuploader = mc.getPotionSpriteUploader();
            Effect effect = effectInstance.getPotion();
            int iconSize = 8;
            TextureAtlasSprite textureatlassprite = potionspriteuploader.getSprite(effect);
            mc.getTextureManager().bindTexture(textureatlassprite.getAtlasTexture().getTextureLocation());
            DisplayEffectsScreen.blit(matrixStack, (int)posX,  (int)posY - 1, iconSize, iconSize, iconSize, textureatlassprite);

            Fonts.consolas.drawTextWithOutline(matrixStack, text, posX + iconSize, posY, -1, 6, 0.05f);
            posY += Fonts.consolas.getHeight(6) + 4;
        }
    }

    private void drawItems(MatrixStack matrixStack, LivingEntity entity, int posX, int posY) {
        // Отображение предметов полностью отключено
    }

    public boolean isValid(Entity e) {
        if (AntiBot.isBot(e)) return false;

        return isInView(e);
    }

    public static void drawMcRect(
            double left,
            double top,
            double right,
            double bottom,
            int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

        bufferbuilder.pos(left, bottom, 1.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, bottom, 1.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, top, 1.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(left, top, 1.0F).color(f, f1, f2, f3).endVertex();
 
    }

    public void glCenteredScale(final float x, final float y, final float w, final float h, final float f) {
        glTranslatef(x + w / 2, y + h / 2, 0);
        glScalef(f, f, 1);
        glTranslatef(-x - w / 2, -y - h / 2, 0);
    }
}


