package polaroid.client.modules.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import polaroid.client.command.friends.FriendStorage;
import polaroid.client.events.EventDisplay;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ModeListSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.client.IMinecraft;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.utils.projections.ProjectionUtil;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.font.Fonts;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.util.*;

@ModuleSystem(name = "NameTags", type = Category.Render, server = ServerCategory.NO, description = "Показывает информацию игрока")
public class NameTags extends Module implements IMinecraft {

    public ModeListSetting elements = new ModeListSetting("Настройки",
            new BooleanSetting("Броня", true),
            new BooleanSetting("Зачарование", true),
            new BooleanSetting("Предметы", false));

    public SliderSetting size = new SliderSetting("Размер", 0.5f, 0.1f, 1.5f, 0.05f);

    private final HashMap<Entity, Vector4f> positions = new HashMap<>();
    float healthAnimation = 0.0f;
    
    // Статический флаг для отключения стандартных ников
    private static boolean hideVanillaNames = false;
    
    public static boolean shouldHideVanillaNames() {
        return hideVanillaNames;
    }

    public NameTags() {
        addSettings(elements, size);
    }
    
    @Override
    public boolean onEnable() {
        hideVanillaNames = true;
        return super.onEnable();
    }
    
    @Override
    public boolean onDisable() {
        hideVanillaNames = false;
        return super.onDisable();
    }

    @Subscribe
    public void onDisplay(EventDisplay event) {
        if (event.getType() != EventDisplay.Type.PRE || mc.world == null) {
            return;
        }

        positions.clear();

        // Собираем позиции всех сущностей
        for (Entity entity : mc.world.getAllEntities()) {
            if (!isValid(entity)) continue;

            // Фильтруем сущности
            if (!(entity instanceof PlayerEntity && entity != mc.player
                    || entity instanceof ItemEntity && elements.getValueByName("Предметы").get()
                    || entity == mc.player && !(mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON)
            )) continue;

            double x = MathUtil.interpolate(entity.getPosX(), entity.lastTickPosX, event.getPartialTicks());
            double y = MathUtil.interpolate(entity.getPosY(), entity.lastTickPosY, event.getPartialTicks());
            double z = MathUtil.interpolate(entity.getPosZ(), entity.lastTickPosZ, event.getPartialTicks());

            Vector3d size = new Vector3d(
                    entity.getBoundingBox().maxX - entity.getBoundingBox().minX,
                    entity.getBoundingBox().maxY - entity.getBoundingBox().minY,
                    entity.getBoundingBox().maxZ - entity.getBoundingBox().minZ
            );

            AxisAlignedBB aabb = new AxisAlignedBB(
                    x - size.x / 2f, y, z - size.z / 2f,
                    x + size.x / 2f, y + size.y, z + size.z / 2f
            );

            Vector4f position = null;
            for (int i = 0; i < 8; i++) {
                Vector2f vector = ProjectionUtil.project(
                        i % 2 == 0 ? aabb.minX : aabb.maxX,
                        (i / 2) % 2 == 0 ? aabb.minY : aabb.maxY,
                        (i / 4) % 2 == 0 ? aabb.minZ : aabb.maxZ
                );

                if (vector != null && vector.x != Float.MAX_VALUE && vector.y != Float.MAX_VALUE) {
                    if (position == null) {
                        position = new Vector4f(vector.x, vector.y, 1, 1.0f);
                    } else {
                        position.x = Math.min(vector.x, position.x);
                        position.y = Math.min(vector.y, position.y);
                        position.z = Math.max(vector.x, position.z);
                        position.w = Math.max(vector.y, position.w);
                    }
                }
            }

            if (position != null) {
                positions.put(entity, position);
            }
        }

        // Рендерим нейм теги
        for (Map.Entry<Entity, Vector4f> entry : positions.entrySet()) {
            Entity entity = entry.getKey();

            if (entity instanceof LivingEntity living) {
                renderPlayerNameTag(event.getMatrixStack(), living, entry.getValue());
            } else if (entity instanceof ItemEntity item) {
                renderItemNameTag(event.getMatrixStack(), item, entry.getValue());
            }
        }
    }

    private void renderPlayerNameTag(MatrixStack ms, LivingEntity entity, Vector4f position) {
        float width = position.z - position.x;

        GL11.glPushMatrix();

        // Формируем имя
        boolean isFriend = FriendStorage.isFriend(entity.getName().getString());
        String friendPrefix = isFriend ? TextFormatting.GREEN + "[F] " : "";
        String creativePrefix = "";
        
        String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();
        Score score = mc.world.getScoreboard().getOrCreateScore(entity.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));

        float hp = entity.getHealth();
        float maxHp = entity.getMaxHealth();

        if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime")
                && (header.contains("анархия") || header.contains("гриферский"))) {
            hp = score.getScorePoints();
            maxHp = 20;
        }

        if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative()) {
            creativePrefix = TextFormatting.GRAY + " [" + TextFormatting.RED + "Creative" + TextFormatting.GRAY + "]";
        } else {
            creativePrefix = TextFormatting.GRAY + " [" + TextFormatting.RED + ((int) hp + (int) entity.getAbsorptionAmount()) + TextFormatting.GRAY + "]";
        }

        healthAnimation = MathUtil.fast(healthAnimation, MathHelper.clamp(hp / maxHp, 0, 1), 10);

        // Получаем третье слово из названия предмета в правой руке (название талисмана/сферы)
        String itemSuffix = "";
        ItemStack offhandItem = entity.getHeldItemOffhand();
        if (!offhandItem.isEmpty() && offhandItem.hasDisplayName()) {
            String displayName = offhandItem.getDisplayName().getString();
            String[] words = displayName.split(" ");
            if (words.length >= 3) {
                // Берем третье слово (название) и делаем его заглавными буквами, добавляем пробел перед
                itemSuffix = " " + TextFormatting.YELLOW + "[" + words[2].toUpperCase() + "]";
            }
        }

        String nameString = friendPrefix + entity.getDisplayName().getString() + creativePrefix + itemSuffix;
        ITextComponent name = new StringTextComponent(nameString);

        glCenteredScale(position.x + width / 2f, position.y - 9, 0, 0, this.size.get());

        float length = mc.fontRenderer.getStringPropertyWidth(name);
        float x1 = position.x + width / 2f - length / 2f - 4;
        float y1 = position.y - 15.5f;
        
        // Зеленый фон для друзей, черный для остальных
        int colorRect = isFriend
                ? ColorUtils.rgba(0, 255, 0, 120) 
                : ColorUtils.rgba(0, 0, 0, 120);

        DisplayUtils.drawRoundedRect(x1, y1, length + 8, 13, 
                new Vector4f(2, 2, 2, 2), colorRect);
        
        mc.fontRenderer.func_243246_a(ms, name, position.x + width / 2f - length / 2f, position.y - 12.5f, -1);

        GL11.glPopMatrix();
        
        if (elements.getValueByName("Броня").get()) {
            drawItems(ms, entity, (int) (position.x + width / 2f), (int) (position.y - 14.5f));
        }
    }

    private void renderItemNameTag(MatrixStack ms, ItemEntity item, Vector4f position) {
        float width = position.z - position.x;
        ITextComponent displayName = new StringTextComponent(
                item.getItem().getDisplayName().getString() + 
                (item.getItem().getCount() < 1 ? "" : " x" + item.getItem().getCount())
        );

        float textLength = Fonts.sfui.getWidth(displayName.getString(), 6);
        float textHeight = Fonts.sfui.getHeight(6);
        float padding = 4.0f;
        float iconSize = 15.0f;

        GL11.glPushMatrix();
        glCenteredScale(position.x + width / 2f - 30, position.y - 15, textLength + iconSize + padding * 3, textHeight + padding * 2, 0.5f);

        float nameX = position.x + width / 2f - (textLength + iconSize + padding) / 2f - 45;
        float nameY = position.y - 15 - textHeight / 2 - padding;

        int bgColor = (item.getItem().getItem() == Items.TOTEM_OF_UNDYING || item.getItem().getItem() == Items.PLAYER_HEAD) 
                ? ColorUtils.rgba(215, 0, 0, 125) 
                : ColorUtils.rgba(20, 20, 20, 155);

        DisplayUtils.drawRoundedRect(nameX, nameY + 0.5f, textLength + padding * 2, 13, 
                new Vector4f(2, 2, 2, 2), bgColor);

        Fonts.sfui.drawText(ms, displayName.getString(), nameX + padding, nameY + padding, -1, 6);

        float iconX = nameX + textLength + padding * 2;

        DisplayUtils.drawRoundedRect(iconX + 2.5f, nameY - 3.5f, 23, 22, 
                new Vector4f(2, 2, 2, 2), bgColor);

        mc.getItemRenderer().renderItemAndEffectIntoGUI(item.getItem(), (int) (iconX + padding / 2) + 5, (int) ((int) (nameY + padding + (textHeight - iconSize) / 2) - 1.5f));
        GL11.glPopMatrix();
    }

    private void drawItems(MatrixStack ms, LivingEntity entity, int posX, int posY) {
        int size = 8;
        int padding = 6;
        float fontHeight = 6;
        List<ItemStack> items = new ArrayList<>();

        ItemStack mainStack = entity.getHeldItemMainhand();
        if (!mainStack.isEmpty()) items.add(mainStack);

        for (ItemStack itemStack : entity.getArmorInventoryList()) {
            if (!itemStack.isEmpty()) items.add(itemStack);
        }

        ItemStack offStack = entity.getHeldItemOffhand();
        if (!offStack.isEmpty()) items.add(offStack);

        posX -= (items.size() * (size + padding)) / 2f;

        for (ItemStack itemStack : items) {
            if (itemStack.isEmpty()) continue;

            GL11.glPushMatrix();
            glCenteredScale(posX, posY - 5, size / 2f, size / 2f, 0.5f);

            float itemX = posX;
            float itemY = posY - 12;
            
            boolean isSkull = itemStack.getItem() instanceof SkullItem;
            boolean isEnchantedTotem = false;

            if (itemStack.getItem() == Items.TOTEM_OF_UNDYING) {
                CompoundNBT tag = itemStack.getTag();
                if (tag != null && tag.contains("Enchantments")) {
                    isEnchantedTotem = true;
                }
            }

            int colorRect = (isSkull || isEnchantedTotem)
                    ? ColorUtils.rgba(215, 0, 0, 125)
                    : (FriendStorage.isFriend(entity.getName().getString()) 
                            ? ColorUtils.rgba(66, 163, 60, 120) 
                            : ColorUtils.rgba(20, 20, 20, 155));

            DisplayUtils.drawRoundedRect(itemX - 3f, itemY - 2.5f, 22, 22, 
                    new Vector4f(2, 2, 2, 2), colorRect);

            mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, posX, (int) (posY - 11.5f));
            mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, posX, (int) (posY - 11.5f), null);
            GL11.glPopMatrix();

            if (elements.getValueByName("Зачарование").get()) {
                ArrayList<String> enchantments = getEnchantment(itemStack);
                int ePosY = (int) (posY - fontHeight);
                for (String enchText : enchantments) {
                    Fonts.consolas.drawText(ms, enchText, posX - 3, ePosY - 5, -1, 5);
                    ePosY -= (int) fontHeight;
                }
            }

            posX += size + padding;
        }
    }

    private ArrayList<String> getEnchantment(ItemStack stack) {
        ArrayList<String> list = new ArrayList<>();
        Item item = stack.getItem();
        if (item instanceof AxeItem || item instanceof BowItem) {
            handleAxeEnchantments(list, stack);
        } else if (item instanceof ArmorItem) {
            handleArmorEnchantments(list, stack);
        } else if (item instanceof SwordItem) {
            handleSwordEnchantments(list, stack);
        }
        return list;
    }

    private void handleArmorEnchantments(ArrayList<String> list, ItemStack stack) {
        int protection = EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, stack);
        int unbreaking = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);
        if (protection > 0) list.add("Pro" + protection);
        if (unbreaking > 0) list.add("Unb" + unbreaking);
    }

    private void handleAxeEnchantments(ArrayList<String> list, ItemStack stack) {
        int sharpness = EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, stack);
        int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
        int unbreaking = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);
        if (sharpness > 0) list.add("Shr" + sharpness);
        if (efficiency > 0) list.add("Eff" + efficiency);
        if (unbreaking > 0) list.add("Unb" + unbreaking);
    }

    private void handleSwordEnchantments(ArrayList<String> list, ItemStack stack) {
        int looting = EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, stack);
        int sharpness = EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, stack);
        int knockback = EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, stack);
        int unbreaking = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);
        if (looting > 0) list.add("Loot" + looting);
        if (sharpness > 0) list.add("Shr" + sharpness);
        if (knockback > 0) list.add("Kno" + knockback);
        if (unbreaking > 0) list.add("Unb" + unbreaking);
    }

    public boolean isValid(Entity e) {
        return e != null && e.isAlive();
    }

    public void glCenteredScale(final float x, final float y, final float w, final float h, final float f) {
        GL11.glTranslatef(x + w / 2, y + h / 2, 0);
        GL11.glScalef(f, f, 1);
        GL11.glTranslatef(-x - w / 2, -y - h / 2, 0);
    }
}


