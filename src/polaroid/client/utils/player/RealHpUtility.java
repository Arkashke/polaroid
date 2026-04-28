package polaroid.client.utils.player;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RealHpUtility {
    
    private static final Map<UUID, PlayerHealthTracker> healthTrackers = new HashMap<>();
    
    private static class PlayerHealthTracker {
        float estimatedHp = 20.0f;
        int lastHurtTime = 0;
        long lastDamageTime = 0;
        long spawnTime = System.currentTimeMillis();
        boolean isDead = false;
        int consecutiveFullHealthTicks = 0;
        
        // Отслеживание атак
        int lastSwingProgress = 0;
        float lastDistanceToLocalPlayer = 0;
    }
    
    public static float getRealHp(AbstractClientPlayerEntity player) {
        if (player == null || player.world == null)
            return 20.0f;
        
        UUID playerId = player.getUniqueID();
        PlayerHealthTracker tracker = healthTrackers.computeIfAbsent(playerId, k -> new PlayerHealthTracker());
        
        // Проверяем, жив ли игрок
        if (!player.isAlive() || player.getHealth() <= 0) {
            tracker.isDead = true;
            tracker.estimatedHp = 0;
            return 0;
        }
        
        // Если игрок был мертв и воскрес - сбрасываем HP
        if (tracker.isDead && player.isAlive()) {
            tracker.estimatedHp = 20.0f;
            tracker.isDead = false;
            tracker.spawnTime = System.currentTimeMillis();
            tracker.lastDamageTime = 0;
            tracker.consecutiveFullHealthTicks = 0;
        }
        
        int currentHurtTime = player.hurtTime;
        long currentTime = System.currentTimeMillis();
        
        // Определяем, получил ли игрок урон
        if (currentHurtTime > 0 && currentHurtTime > tracker.lastHurtTime) {
            // Игрок только что получил урон
            long timeSinceLastDamage = currentTime - tracker.lastDamageTime;
            
            // Вычисляем примерный урон на основе hurtTime
            // hurtTime = 10 обычно означает полный урон
            float estimatedDamage = estimateDamageFromHurtTime(player, currentHurtTime);
            
            // Применяем урон
            tracker.estimatedHp -= estimatedDamage;
            tracker.lastDamageTime = currentTime;
            tracker.consecutiveFullHealthTicks = 0;
            
            // Ограничиваем минимум
            if (tracker.estimatedHp < 0.5f) {
                tracker.estimatedHp = 0.5f;
            }
        }
        
        // Проверяем регенерацию
        if (currentHurtTime == 0 && tracker.lastHurtTime == 0) {
            tracker.consecutiveFullHealthTicks++;
            
            // Если игрок долго не получал урон, возможно у него полное HP
            if (tracker.consecutiveFullHealthTicks > 100) { // ~5 секунд
                // Проверяем эффекты регенерации
                if (player.isPotionActive(Effects.REGENERATION)) {
                    tracker.estimatedHp += 0.1f; // Медленная регенерация
                }
                
                // Естественная регенерация (если сытость полная)
                if (tracker.consecutiveFullHealthTicks > 160) { // ~8 секунд
                    tracker.estimatedHp += 0.05f;
                }
            }
            
            // Если прошло много времени без урона, предполагаем полное HP
            if (currentTime - tracker.lastDamageTime > 30000) { // 30 секунд
                tracker.estimatedHp = 20.0f;
            }
        }
        
        // Ограничиваем HP в пределах 0.5-20
        tracker.estimatedHp = Math.max(0.5f, Math.min(20.0f, tracker.estimatedHp));
        
        tracker.lastHurtTime = currentHurtTime;
        
        return tracker.estimatedHp;
    }
    
    private static float estimateDamageFromHurtTime(AbstractClientPlayerEntity player, int hurtTime) {
        // Базовый урон на основе hurtTime
        // hurtTime = 10 обычно означает сильный удар
        float baseDamage = 1.0f;
        
        if (hurtTime >= 10) {
            baseDamage = 3.0f; // Сильный удар (меч, топор)
        } else if (hurtTime >= 7) {
            baseDamage = 2.0f; // Средний удар
        } else if (hurtTime >= 4) {
            baseDamage = 1.0f; // Слабый удар
        } else {
            baseDamage = 0.5f; // Очень слабый урон
        }
        
        // Проверяем расстояние до локального игрока
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            float distance = player.getDistance(mc.player);
            
            // Если игрок близко к нам, возможно мы его ударили
            if (distance < 6.0f) {
                // Проверяем, атаковали ли мы недавно
                if (mc.player.swingProgress > 0 || mc.player.isSwingInProgress) {
                    // Вычисляем урон на основе нашего оружия
                    baseDamage = estimateWeaponDamage(mc.player);
                }
            }
        }
        
        // Учитываем броню игрока (визуально)
        int armorValue = player.getTotalArmorValue();
        float armorReduction = 1.0f - (armorValue * 0.04f); // Каждая единица брони снижает урон на 4%
        armorReduction = Math.max(0.2f, armorReduction); // Минимум 20% урона проходит
        
        return baseDamage * armorReduction;
    }
    
    private static float estimateWeaponDamage(PlayerEntity attacker) {
        // Пытаемся определить урон оружия в руке
        if (attacker.getHeldItemMainhand().isEmpty()) {
            return 1.0f; // Урон рукой
        }
        
        String itemName = attacker.getHeldItemMainhand().getItem().toString().toLowerCase();
        
        // Мечи
        if (itemName.contains("diamond_sword")) return 7.0f;
        if (itemName.contains("iron_sword")) return 6.0f;
        if (itemName.contains("stone_sword")) return 5.0f;
        if (itemName.contains("golden_sword")) return 4.0f;
        if (itemName.contains("wooden_sword")) return 4.0f;
        if (itemName.contains("netherite_sword")) return 8.0f;
        
        // Топоры
        if (itemName.contains("diamond_axe")) return 9.0f;
        if (itemName.contains("iron_axe")) return 9.0f;
        if (itemName.contains("stone_axe")) return 9.0f;
        if (itemName.contains("netherite_axe")) return 10.0f;
        
        // По умолчанию
        return 2.0f;
    }
    
    public static void resetPlayer(UUID playerId) {
        healthTrackers.remove(playerId);
    }
    
    public static void cleanupOldData() {
        long currentTime = System.currentTimeMillis();
        healthTrackers.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().spawnTime > 300000 // 5 минут
        );
    }
    
    // Метод для ручной установки HP (если известно точное значение)
    public static void setPlayerHp(UUID playerId, float hp) {
        PlayerHealthTracker tracker = healthTrackers.computeIfAbsent(playerId, k -> new PlayerHealthTracker());
        tracker.estimatedHp = Math.max(0.5f, Math.min(20.0f, hp));
    }
}


