package polaroid.client.modules.impl.render;

import com.google.common.eventbus.Subscribe;
import net.minecraft.util.ResourceLocation;
import polaroid.client.events.AttackEvent;
import polaroid.client.events.EventDisplay;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.utils.math.Vector4i;
import polaroid.client.utils.projections.ProjectionUtil;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.font.Fonts;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static net.minecraft.client.renderer.WorldRenderer.frustum;

@ModuleSystem(name = "Particle", type = Category.Render, server = ServerCategory.NO, description = "Частицы при ударе энтити")
public class Particles extends Module {

    private final ModeSetting setting = new ModeSetting("Текстура", "Орбизы", "Орбизы", "Снежинки", "Сердечки", "Звездочки");
    private final SliderSetting value = new SliderSetting("Количество", 20.0f, 1.0f, 50.0f, 1.0f);
    private final ModeSetting colorMode = new ModeSetting("Цвет", "Клиентский", "Клиентский", "Кастомный");
    private final polaroid.client.modules.settings.impl.ColorSetting customColor = new polaroid.client.modules.settings.impl.ColorSetting("Свой цвет", -1).setVisible(() -> colorMode.is("Кастомный"));
    private final SliderSetting speed = new SliderSetting("Скорость", 3.0f, 0.5f, 10.0f, 0.5f);
    private final SliderSetting lifeTime = new SliderSetting("Длительность", 5000f, 1000f, 10000f, 500f);
    private final SliderSetting spreadStrength = new SliderSetting("Сила разлета", 3.0f, 1.0f, 10.0f, 0.5f);
    private final CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();

    public Particles() {
        addSettings(setting, value, colorMode, customColor, speed, lifeTime, spreadStrength);
    }

    private boolean isInView(Vector3d pos) {
        frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x,
                mc.getRenderManager().info.getProjectedView().y,
                mc.getRenderManager().info.getProjectedView().z);
        return frustum.isBoundingBoxInFrustum(new AxisAlignedBB(pos.add(-0.2, -0.2, -0.2), pos.add(0.2, 0.2, 0.2)));
    }

    @Subscribe
    private void onUpdate(AttackEvent e) {
        if (e.entity == mc.player) return;
        if (e.entity instanceof LivingEntity livingEntity) {
            for (int i = 0; i < value.get(); i++) {
                particles.add(new Particle(livingEntity.getPositon(mc.getRenderPartialTicks()).add(0, livingEntity.getHeight() / 2f, 0)));
            }
        }
    }

    @Subscribe
    private void onDisplay(EventDisplay e) {
        if (mc.player == null || mc.world == null || e.getType() != EventDisplay.Type.PRE) {
            return;
        }

        for (Particle p : particles) {
            long elapsed = System.currentTimeMillis() - p.time;
            
            // Плавная анимация уменьшения размера от 1 до 0
            float size = 1 - (elapsed / (float)lifeTime.get());
            size = Math.max(0, Math.min(1, size)); // Clamp между 0 и 1
            
            // Удаляем частицу только когда она полностью исчезла
            if (size <= 0) {
                particles.remove(p);
                continue;
            }
            
            // Проверяем дистанцию для оптимизации
            if (mc.player.getPositionVec().distanceTo(p.pos) > 50) {
                particles.remove(p);
                continue;
            }
            
            p.update();
            Vector2f pos = ProjectionUtil.project(p.pos.x, p.pos.y, p.pos.z);
            
            // Определяем цвет с плавным затуханием альфы
            Vector4i colors;
            if (colorMode.is("Кастомный")) {
                int color = ColorUtils.setAlpha(customColor.get(), (int)(125 * size));
                colors = new Vector4i(color, color, color, color);
            } else {
                colors = new Vector4i(
                    ColorUtils.setAlpha(Theme.getColor(0, 1.0F), (int)(125 * size)), 
                    ColorUtils.setAlpha(Theme.getColor(90, 1.0F), (int)(125 * size)), 
                    ColorUtils.setAlpha(Theme.getColor(180, 1.0F), (int)(125 * size)), 
                    ColorUtils.setAlpha(Theme.getColor(270, 1.0F), (int)(125 * size))
                );
            }

            float renderSize = 25 * size;
            switch (setting.get()) {
                case "Орбизы" -> {
                    DisplayUtils.drawImageAlpha(new ResourceLocation("polaroid/images/modules/particle/glow.png"), pos.x - renderSize / 2, pos.y - renderSize / 2, renderSize, renderSize, colors);
                }
                case "Снежинки" -> {
                    DisplayUtils.drawImageAlpha(new ResourceLocation("polaroid/images/modules/particle/snow.png"), pos.x - renderSize / 2, pos.y - renderSize / 2, renderSize, renderSize, colors);
                }
                case "Сердечки" -> {
                    DisplayUtils.drawImageAlpha(new ResourceLocation("polaroid/images/modules/particle/heart1.png"), pos.x - renderSize / 2, pos.y - renderSize / 2, renderSize, renderSize, colors);
                }
                case "Звездочки" -> {
                    DisplayUtils.drawImageAlpha(new ResourceLocation("polaroid/images/modules/particle/star.png"), pos.x - renderSize / 2, pos.y - renderSize / 2, renderSize, renderSize, colors);
                }

            }
        }

    }

    private class Particle {
        private Vector3d pos;
        private final Vector3d end;
        private final long time;

        private float alpha;


        public Particle(Vector3d pos) {
            this.pos = pos;
            float spread = spreadStrength.get();
            end = pos.add(
                -ThreadLocalRandom.current().nextFloat(-spread, spread), 
                -ThreadLocalRandom.current().nextFloat(-spread, spread), 
                -ThreadLocalRandom.current().nextFloat(-spread, spread)
            );
            time = System.currentTimeMillis();
        }

        public void update() {
            alpha = MathUtil.fast(alpha, 1, 10);
            pos = MathUtil.fast(pos, end, speed.get() / 10f);

        }


    }

}


