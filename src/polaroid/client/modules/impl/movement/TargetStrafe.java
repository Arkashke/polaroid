package polaroid.client.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import polaroid.client.Polaroid;
import polaroid.client.events.EventPacket;
import polaroid.client.events.EventUpdate;
import polaroid.client.events.MovingEvent;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.impl.combat.Aura;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

@ModuleSystem(name = "TargetStrafe", type = Category.Movement, server = ServerCategory.NO, description = "Автоматический стрейф вокруг цели")
public class TargetStrafe extends Module {
    
    private final SliderSetting speed = new SliderSetting("Скорость", 1.0f, 0.1f, 2.0f, 0.05f);
    private final SliderSetting radius = new SliderSetting("Радиус", 1.0f, 0.1f, 6.0f, 0.05f);
    private final BooleanSetting jump = new BooleanSetting("Прыгать", true);
    private final BooleanSetting autoJump = new BooleanSetting("Авто-прыжок", true);
    
    private float side = 1;
    private LivingEntity target = null;
    private double lastSpeed = 0.0;
    private boolean isStrafing = false;

    public TargetStrafe() {
        addSettings(speed, radius, jump, autoJump);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        if (e.getType() == EventPacket.Type.RECEIVE && e.getPacket() instanceof SPlayerPositionLookPacket) {
            lastSpeed = 0.0;
        }
    }

    @Subscribe
    public void onMotion(MovingEvent event) {
        if (mc.player == null || mc.world == null || !this.isState()) return;

        // Проверка состояния игрока
        if (isInvalidPlayerState()) return;

        // Получаем цель из Aura
        Aura aura = Polaroid.getInstance().getFunctionRegistry().getAura();
        if (aura == null || !aura.isState()) {
            isStrafing = false;
            return;
        }
        
        LivingEntity auraTarget = aura.getTarget();
        if (auraTarget == null) {
            isStrafing = false;
            return;
        }

        target = auraTarget;

        // Обновляем направление при столкновении или нажатии A/D
        handleInput();

        // Вычисляем позицию цели
        double targetX = target.getPosX();
        double targetZ = target.getPosZ();

        // Вычисляем угол стрейфа
        double deltaX = mc.player.getPosX() - targetX;
        double deltaZ = mc.player.getPosZ() - targetZ;
        double baseAngle = Math.atan2(deltaZ, deltaX);

        // Угол стрейфа (90 градусов от цели)
        double strafeAngle = baseAngle + side * Math.PI / 2;

        // Вычисляем позицию на окружности
        double circleX = targetX + radius.get() * Math.cos(strafeAngle);
        double circleZ = targetZ + radius.get() * Math.sin(strafeAngle);

        // Вычисляем вектор движения
        double dx = circleX - mc.player.getPosX();
        double dz = circleZ - mc.player.getPosZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist < 0.01) return;

        // Вычисляем скорость (используем настройку скорости)
        double currentSpeed = speed.get();
        currentSpeed = lastSpeed + (currentSpeed - lastSpeed) * 0.3;
        lastSpeed = currentSpeed;

        // Применяем движение к игроку
        double motionX = (dx / dist) * currentSpeed;
        double motionZ = (dz / dist) * currentSpeed;

        // Применяем движение
        event.getMotion().x = motionX;
        event.getMotion().z = motionZ;

        isStrafing = true;
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (!this.isState() || target == null || !target.isAlive()) return;

        if (jump.get() && autoJump.get() && mc.player.isOnGround()) {
            mc.player.jump();
        }
    }

    private void handleInput() {
        boolean left = InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_A);
        boolean right = InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_D);

        if (mc.player.collidedHorizontally) {
            side *= -1;
        }

        if (left) side = 1;
        if (right) side = -1;
    }

    private boolean isInvalidPlayerState() {
        return mc.player == null || mc.world == null
                || mc.player.isSneaking()
                || mc.player.isElytraFlying()
                || mc.player.isInWater()
                || mc.player.isInLava()
                || mc.player.abilities.isFlying
                || mc.player.isPotionActive(Effects.LEVITATION)
                || isInWebOrSoulSand();
    }

    private boolean isInWebOrSoulSand() {
        BlockPos pos = new BlockPos(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ());
        return mc.world.getBlockState(pos).getMaterial() == Material.WEB
                || mc.world.getBlockState(pos.down()).getBlock() == Blocks.SOUL_SAND;
    }

    @Override
    public boolean onEnable() {
        lastSpeed = 0.0;
        target = null;
        isStrafing = false;
        super.onEnable();
        return false;
    }

    @Override
    public boolean onDisable() {
        target = null;
        isStrafing = false;
        super.onDisable();
        return false;
    }
}


