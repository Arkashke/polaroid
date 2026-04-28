package polaroid.client.modules.impl.player;

import polaroid.client.Polaroid;
import polaroid.client.events.EventMotion;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleRegistry;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.impl.combat.Aura;
import polaroid.client.modules.impl.render.SwingAnimation;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import com.google.common.eventbus.Subscribe;

import java.util.Random;

@ModuleSystem(name = "AntiAim", type = Category.Player, server = ServerCategory.NO, description = "Дефолт антиаим")
public class AntiAim extends Module {
    private final BooleanSetting realBoolean = new BooleanSetting("\u041c\u0435\u043d\u044f\u0442\u044c \u0445\u0438\u0442\u0431\u043e\u043a\u0441", false);
    private final BooleanSetting randomReal = new BooleanSetting("\u0410\u043d\u0442\u0438-\u0411\u0440\u0443\u0442\u0444\u043e\u0440\u0441", true).setVisible(this::lambda$new$0);
    private final BooleanSetting fakeBoolean = new BooleanSetting("\u0412\u0438\u0437\u0443\u0430\u043b\u044c\u043d\u044b\u0435 \u0410\u0410", true);
    private final ModeSetting fakeModeYaw = new ModeSetting("\u041c\u0435\u043d\u044f\u0442\u044c Yaw", "Jitter", "Jitter", "Static", "Random", "Defense").setVisible(this::lambda$new$1);
    private final SliderSetting yawSlider = new SliderSetting("\u0423\u0433\u043e\u043b Yaw", 60.0f, 1.0f, 70.0f, 1.0f).setVisible(this::lambda$new$2);
    private final ModeSetting fakeModePitch = new ModeSetting("\u041c\u0435\u043d\u044f\u0442\u044c Pitch", "Defense", "Defense", "Custom").setVisible(this::lambda$new$3);
    private final SliderSetting pitchSlider = new SliderSetting("\u0423\u0433\u043e\u043b Pitch", 65.0f, 0.0f, 90.0f, 1.0f).setVisible(this::lambda$new$4);
    private final BooleanSetting zeroPitch = new BooleanSetting("Zero pitch on land", false).setVisible(this::lambda$new$5);
    private final BooleanSetting chivoBlyat = new BooleanSetting("\u041e\u0442\u043e\u0431\u0440\u0430\u0436\u0430\u0442\u044c \u0443 \u0432\u0441\u0435\u0445", false).setVisible(this::lambda$new$6);
    private final Random random = new Random();
    float yaw = 0.0f;
    float pitch = 0.0f;
    long timeLanded = 0L;
    int delayTime = 500;
    boolean can = true;

    public AntiAim() {
        this.addSettings(this.realBoolean, this.randomReal, this.fakeBoolean, this.fakeModeYaw, this.fakeModePitch, this.zeroPitch, this.yawSlider, this.pitchSlider, this.chivoBlyat);
    }

    @Subscribe
    public void onMotion(EventMotion eventMotion) {
        block25:
        {
            block24:
            {
                if (!Polaroid.getInstance().getFunctionRegistry().getAura().isState()) break block24;
                if (!Polaroid.getInstance().getFunctionRegistry().getAura().isState()) break block25;
                if (Polaroid.getInstance().getFunctionRegistry().getAura().getTarget() != null) break block25;
            }
            if (((Boolean) this.fakeBoolean.get()).booleanValue()) {
                if (AntiAim.mc.gameSettings.keyBindUseItem.pressed || AntiAim.mc.gameSettings.keyBindAttack.pressed || AntiAim.mc.currentScreen != null) {
                    this.can = false;
                    return;
                }
                this.can = true;
                if (this.fakeModeYaw.is("Jitter")) {
                    if (AntiAim.mc.player.ticksExisted % 2 == 0) {
                        AntiAim.mc.player.rotationYawHead = AntiAim.mc.player.renderYawOffset = AntiAim.mc.player.rotationYaw + ((Float) this.yawSlider.get()).floatValue() + 180.0f;
                        this.yaw = AntiAim.mc.player.renderYawOffset;
                    } else {
                        AntiAim.mc.player.rotationYawHead = AntiAim.mc.player.renderYawOffset = AntiAim.mc.player.rotationYaw - ((Float) this.yawSlider.get()).floatValue() + 180.0f;
                        this.yaw = AntiAim.mc.player.renderYawOffset;
                    }
                }
                if (this.fakeModeYaw.is("Static")) {
                    AntiAim.mc.player.rotationYawHead = AntiAim.mc.player.renderYawOffset = AntiAim.mc.player.rotationYaw + 180.0f;
                    this.yaw = AntiAim.mc.player.renderYawOffset;
                }
                if (this.fakeModeYaw.is("Defense")) {
                    AntiAim.mc.player.rotationYawHead = AntiAim.mc.player.renderYawOffset = AntiAim.mc.player.rotationYaw + 180.0f;
                    this.yaw = AntiAim.mc.player.renderYawOffset;
                    if (AntiAim.mc.player.ticksExisted % this.randomInt(2, 6) == 0) {
                        AntiAim.mc.player.rotationYawHead = AntiAim.mc.player.renderYawOffset = AntiAim.mc.player.rotationYaw + (float) this.randomInt(12, 60) + 200.0f;
                        this.yaw = AntiAim.mc.player.renderYawOffset;
                    } else {
                        AntiAim.mc.player.rotationYawHead = AntiAim.mc.player.renderYawOffset = AntiAim.mc.player.rotationYaw - (float) this.randomInt(12, 60) + 200.0f;
                        this.yaw = AntiAim.mc.player.renderYawOffset;
                    }
                }
                if (this.fakeModeYaw.is("Random")) {
                    int n = this.randomInt(1, 180);
                    if (this.random.nextBoolean()) {
                        AntiAim.mc.player.rotationYawHead = AntiAim.mc.player.renderYawOffset = AntiAim.mc.player.rotationYaw + 180.0f + (float) n;
                        this.yaw = AntiAim.mc.player.renderYawOffset;
                    } else {
                        AntiAim.mc.player.rotationYawHead = AntiAim.mc.player.renderYawOffset = AntiAim.mc.player.rotationYaw + 180.0f - (float) n;
                        this.yaw = AntiAim.mc.player.renderYawOffset;
                    }
                }
                if (this.fakeModePitch.is("Custom")) {
                    this.pitch = AntiAim.mc.player.rotationPitchHead = ((Float) this.pitchSlider.get()).floatValue();
                }
                if (this.fakeModePitch.is("Defense")) {
                    this.pitch = AntiAim.mc.player.rotationPitchHead = ((Float) this.pitchSlider.get()).floatValue();
                    if (AntiAim.mc.player.ticksExisted % this.randomInt(4, 12) == 0) {
                        AntiAim.mc.player.rotationPitchHead = -65.0f;
                        this.pitch = -65.0f;
                    }
                }
                if (((Boolean) this.zeroPitch.get()).booleanValue()) {
                    if (AntiAim.mc.player.isOnGround()) {
                        if (this.timeLanded == 0L) {
                            this.timeLanded = System.currentTimeMillis();
                        }
                        if (System.currentTimeMillis() - this.timeLanded <= (long) this.delayTime) {
                            AntiAim.mc.player.rotationPitchHead = 0.0f;
                            this.pitch = 0.0f;
                        }
                    } else {
                        this.timeLanded = 0L;
                    }
                }
                if (((Boolean) this.chivoBlyat.get()).booleanValue() && this.can) {
                    eventMotion.setPitch(this.pitch);
                    eventMotion.setYaw(this.yaw);
                }
            }
        }


    }
    @Subscribe
    public void onUpdate(EventUpdate eventUpdate) {
        if (((Boolean)this.realBoolean.get()).booleanValue()) {
            if (AntiAim.mc.player.isInWater() || AntiAim.mc.gameSettings.keyBindUseItem.pressed || AntiAim.mc.gameSettings.keyBindAttack.pressed || AntiAim.mc.currentScreen != null) {
                AntiAim.mc.player.stopFallFlying();
                return;
            }
            int n = 4;
            if (((Boolean)this.randomReal.get()).booleanValue()) {
                n = this.randomInt(4, 8);
            }
            if (AntiAim.mc.player.ticksExisted % n == 0) {
                AntiAim.mc.player.startFallFlying();
            } else {
                AntiAim.mc.player.stopFallFlying();
            }
        }
    }

    @Subscribe
    private void onWalking(EventMotion e) {

        mc.player.rotationYawHead = AntiAim.mc.player.rotationYawHead;
        mc.player.renderYawOffset = AntiAim.mc.player.rotationYawHead;
        mc.player.rotationPitchHead = AntiAim.mc.player.rotationYawHead;


    }


public void reset() {
        this.yaw = AntiAim.mc.player.rotationYaw;
        this.pitch = AntiAim.mc.player.rotationPitch;
    }

    @Override
    public boolean onDisable() {
        this.reset();
        super.onDisable();
        return false;
    }

    private int randomInt(int n, int n2) {
        return this.random.nextInt(n2 - n + 1) + n;
    }

    private Boolean lambda$new$6() {
        return (Boolean)this.fakeBoolean.get();
    }

    private Boolean lambda$new$5() {
        return (Boolean)this.fakeBoolean.get();
    }

    private Boolean lambda$new$4() {
        return (Boolean)this.fakeBoolean.get();
    }

    private Boolean lambda$new$3() {
        return (Boolean)this.fakeBoolean.get();
    }

    private Boolean lambda$new$2() {
        return (Boolean)this.fakeBoolean.get() != false && this.fakeModeYaw.is("Jitter");
    }

    private Boolean lambda$new$1() {
        return (Boolean)this.fakeBoolean.get();
    }

    private Boolean lambda$new$0() {
        return (Boolean)this.realBoolean.get();
    }

}


