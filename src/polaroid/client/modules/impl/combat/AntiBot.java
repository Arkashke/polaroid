package polaroid.client.modules.impl.combat;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.events.EventChangeWorld;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

@ModuleSystem(name = "AntiBot", type = Category.Combat, server = ServerCategory.NO, description = "Удаляет ботов с сервера")
public class AntiBot extends Module {
    
    public static List<Entity> botList = new ArrayList<>();
    public static List<PlayerEntity> entitiesToRemove = new ArrayList<>();

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.world == null) return;

        for (PlayerEntity entity : mc.world.getPlayers()) {
            if (mc.player == entity) continue;

            // Проверка на бота по критериям:
            // 1. Все слоты брони заполнены
            // 2. Вся броня зачаровываемая
            // 3. Нет предмета в оффхенде
            // 4. Броня кожаная или железная
            // 5. Есть предмет в основной руке
            // 6. Броня не повреждена
            
            boolean hasFullArmor = !entity.inventory.armorInventory.get(0).isEmpty()
                    && !entity.inventory.armorInventory.get(1).isEmpty()
                    && !entity.inventory.armorInventory.get(2).isEmpty()
                    && !entity.inventory.armorInventory.get(3).isEmpty();

            if (!hasFullArmor) {
                if (botList.contains(entity)) {
                    botList.remove(entity);
                }
                continue;
            }

            boolean allEnchantable = entity.inventory.armorInventory.get(0).isEnchantable()
                    && entity.inventory.armorInventory.get(1).isEnchantable()
                    && entity.inventory.armorInventory.get(2).isEnchantable()
                    && entity.inventory.armorInventory.get(3).isEnchantable();

            boolean noOffhand = entity.getHeldItemOffhand().getItem() == Items.AIR;

            boolean isLeatherOrIron = (
                    entity.inventory.armorInventory.get(0).getItem() == Items.LEATHER_BOOTS
                            || entity.inventory.armorInventory.get(1).getItem() == Items.LEATHER_LEGGINGS
                            || entity.inventory.armorInventory.get(2).getItem() == Items.LEATHER_CHESTPLATE
                            || entity.inventory.armorInventory.get(3).getItem() == Items.LEATHER_HELMET
                            || entity.inventory.armorInventory.get(0).getItem() == Items.IRON_BOOTS
                            || entity.inventory.armorInventory.get(1).getItem() == Items.IRON_LEGGINGS
                            || entity.inventory.armorInventory.get(2).getItem() == Items.IRON_CHESTPLATE
                            || entity.inventory.armorInventory.get(3).getItem() == Items.IRON_HELMET
            );

            boolean hasMainHandItem = entity.getHeldItemMainhand().getItem() != Items.AIR;

            boolean noDamage = !entity.inventory.armorInventory.get(0).isDamaged()
                    && !entity.inventory.armorInventory.get(1).isDamaged()
                    && !entity.inventory.armorInventory.get(2).isDamaged()
                    && !entity.inventory.armorInventory.get(3).isDamaged();

            // Если все условия выполнены - это бот
            if (allEnchantable && noOffhand && isLeatherOrIron && hasMainHandItem && noDamage) {
                if (!botList.contains(entity)) {
                    botList.add(entity);
                    removeBotFromWorld(entity);
                }
            } else {
                if (botList.contains(entity)) {
                    botList.remove(entity);
                }
            }
        }

        entitiesToRemove.clear();
    }

    @Subscribe
    public void onWorldChange(EventChangeWorld event) {
        botList.clear();
        entitiesToRemove.clear();
    }

    private void removeBotFromWorld(PlayerEntity entity) {
        entitiesToRemove.add(entity);
        // Просто добавляем в список ботов, физически не удаляем сущность
        // Другие модули будут проверять isBot() и игнорировать этих игроков
    }

    public static boolean isBot(Entity entity) {
        return entity instanceof PlayerEntity && botList.contains(entity);
    }

    public static boolean checkBot(LivingEntity entity) {
        return entity instanceof PlayerEntity && botList.contains(entity);
    }

    @Override
    public boolean onDisable() {
        botList.clear();
        entitiesToRemove.clear();
        return super.onDisable();
    }
}


