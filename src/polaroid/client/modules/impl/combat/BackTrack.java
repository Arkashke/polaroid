package polaroid.client.modules.impl.combat;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.SliderSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

@ModuleSystem(name = "BackTrack", type = Category.Combat, server = ServerCategory.NO, description = "Откатывает позицию игроков назад")
public class BackTrack extends Module {

    private final SliderSetting delay = new SliderSetting("Задержка", 500, 0, 1000, 50);
    private final Map<PlayerEntity, Queue<PositionData>> positionHistory = new HashMap<>();

    public BackTrack() {
        addSettings(delay);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;

            Queue<PositionData> history = positionHistory.computeIfAbsent(player, k -> new LinkedList<>());
            
            // Добавляем текущую позицию
            history.add(new PositionData(player.getPositionVec(), System.currentTimeMillis()));

            // Удаляем старые позиции
            while (!history.isEmpty() && System.currentTimeMillis() - history.peek().time > delay.get()) {
                history.poll();
            }
        }
    }

    public Vector3d getBackTrackedPosition(PlayerEntity player) {
        Queue<PositionData> history = positionHistory.get(player);
        if (history != null && !history.isEmpty()) {
            return history.peek().position;
        }
        return player.getPositionVec();
    }

    @Override
    public boolean onDisable() {
        positionHistory.clear();
        return super.onDisable();
    }

    private static class PositionData {
        final Vector3d position;
        final long time;

        PositionData(Vector3d position, long time) {
            this.position = position;
            this.time = time;
        }
    }
}


