/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package polaroid.client.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import polaroid.client.events.EventLivingUpdate;
import polaroid.client.events.EventMotion;
import polaroid.client.events.EventPacket;
import polaroid.client.events.WorldEvent;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.font.Fonts;
import net.minecraft.client.MainWindow;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

@ModuleSystem(name="FreeCam", description="Позволяет летать по миру в режиме свободной камеры", type=Category.Player, server = ServerCategory.NO)
public class FreeCam
        extends Module {
    private final SliderSetting speed = new SliderSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u043f\u043e XZ", 1.0f, 0.1f, 5.0f, 0.05f);
    private final SliderSetting motionY = new SliderSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u043f\u043e Y", 0.5f, 0.1f, 1.0f, 0.05f);
    private final BooleanSetting noflightkick = new BooleanSetting("\u0410\u043d\u0442\u0438-\u043a\u0438\u043a", false);
    private Vector3d clientPosition = null;
    private RemoteClientPlayerEntity fakePlayer;
    private boolean oldIsFlying;

    public FreeCam() {
        this.addSettings(this.speed, this.motionY, this.noflightkick);
    }

    @Subscribe
    public void onPacket(EventPacket eventPacket) {
        if (FreeCam.mc.player == null || !((Boolean)this.noflightkick.get()).booleanValue()) {
            return;
        }
        IPacket<?> iPacket = eventPacket.getPacket();
        if (iPacket instanceof CPlayerPacket) {
            CPlayerPacket cPlayerPacket = (CPlayerPacket)iPacket;
            if (cPlayerPacket.moving) {
                cPlayerPacket.x = FreeCam.mc.player.getPosX();
                cPlayerPacket.y = FreeCam.mc.player.getPosY();
                cPlayerPacket.z = FreeCam.mc.player.getPosZ();
            }
            cPlayerPacket.onGround = FreeCam.mc.player.isOnGround();
            if (cPlayerPacket.rotating) {
                cPlayerPacket.yaw = FreeCam.mc.player.rotationYaw;
                cPlayerPacket.pitch = FreeCam.mc.player.rotationPitch;
            }
        }
    }

    @Subscribe
    public void onLivingUpdate(EventLivingUpdate eventLivingUpdate) {
        if (FreeCam.mc.player != null) {
            FreeCam.mc.player.noClip = true;
            FreeCam.mc.player.setOnGround(true);
            Vector3d vector3d = new Vector3d(0.0, 0.0, 0.0);
            if (FreeCam.mc.gameSettings.keyBindForward.isKeyDown()) {
                vector3d = vector3d.add(new Vector3d(-MathHelper.sin(FreeCam.mc.player.rotationYaw * ((float)Math.PI / 180)) * ((Float)this.speed.get()).floatValue(), 0.0, MathHelper.cos(FreeCam.mc.player.rotationYaw * ((float)Math.PI / 180)) * ((Float)this.speed.get()).floatValue()));
            }
            if (FreeCam.mc.gameSettings.keyBindBack.isKeyDown()) {
                vector3d = vector3d.add(new Vector3d(MathHelper.sin(FreeCam.mc.player.rotationYaw * ((float)Math.PI / 180)) * ((Float)this.speed.get()).floatValue(), 0.0, -MathHelper.cos(FreeCam.mc.player.rotationYaw * ((float)Math.PI / 180)) * ((Float)this.speed.get()).floatValue()));
            }
            if (FreeCam.mc.gameSettings.keyBindLeft.isKeyDown()) {
                vector3d = vector3d.add(new Vector3d(-MathHelper.sin((FreeCam.mc.player.rotationYaw - 90.0f) * ((float)Math.PI / 180)) * ((Float)this.speed.get()).floatValue(), 0.0, MathHelper.cos((FreeCam.mc.player.rotationYaw - 90.0f) * ((float)Math.PI / 180)) * ((Float)this.speed.get()).floatValue()));
            }
            if (FreeCam.mc.gameSettings.keyBindRight.isKeyDown()) {
                vector3d = vector3d.add(new Vector3d(-MathHelper.sin((FreeCam.mc.player.rotationYaw + 90.0f) * ((float)Math.PI / 180)) * ((Float)this.speed.get()).floatValue(), 0.0, MathHelper.cos((FreeCam.mc.player.rotationYaw + 90.0f) * ((float)Math.PI / 180)) * ((Float)this.speed.get()).floatValue()));
            }
            if (FreeCam.mc.gameSettings.keyBindJump.isKeyDown()) {
                vector3d = vector3d.add(new Vector3d(0.0, ((Float)this.motionY.get()).floatValue(), 0.0));
            }
            if (FreeCam.mc.gameSettings.keyBindSneak.isKeyDown()) {
                vector3d = vector3d.add(new Vector3d(0.0, -((Float)this.motionY.get()).floatValue(), 0.0));
            }
            FreeCam.mc.player.setMotion(vector3d);
            FreeCam.mc.player.abilities.isFlying = true;
        }
    }

    @Subscribe
    public void onMotion(EventMotion eventMotion) {
        if (FreeCam.mc.player != null && FreeCam.mc.player.ticksExisted % 10 == 0) {
            FreeCam.mc.player.connection.sendPacket(new CPlayerPacket(FreeCam.mc.player.isOnGround()));
        }
        if (FreeCam.mc.player != null) {
            eventMotion.cancel();
        }
        if (FreeCam.mc.player.isSprinting()) {
            FreeCam.mc.player.setSprinting(true);
        }
    }

    @Subscribe
    public void onRender(WorldEvent worldEvent) {
        int n = (int)(this.fakePlayer.getPosX() - FreeCam.mc.player.getPosX());
        int n2 = (int)(this.fakePlayer.getPosY() - FreeCam.mc.player.getPosY());
        int n3 = (int)(this.fakePlayer.getPosZ() - FreeCam.mc.player.getPosZ());
        MainWindow mainWindow = mc.getMainWindow();
        String string = "X:" + n + " Y:" + n2 + " Z:" + n3;
        int n4 = ColorUtils.setAlpha(-1, 255);
        Fonts.sfui.drawText(new MatrixStack(), string, (float)mainWindow.getScaledWidth() / 2.0f, (float)mainWindow.getScaledHeight() / 2.0f + 10.0f, -1, 5.0f);
    }

    @Override
    public boolean onEnable() {
        if (FreeCam.mc.player == null) {
            return false;
        }
        this.clientPosition = FreeCam.mc.player.getPositionVec();
        this.oldIsFlying = FreeCam.mc.player.abilities.isFlying;
        FreeCam.mc.player.abilities.isFlying = true;
        this.spawnFakePlayer();
        super.onEnable();
        return false;
    }

    @Override
    public boolean onDisable() {
        if (FreeCam.mc.player == null) {
            return false;
        }
        FreeCam.mc.player.abilities.isFlying = this.oldIsFlying;
        if (this.clientPosition != null) {
            FreeCam.mc.player.setPositionAndRotation(this.clientPosition.x, this.clientPosition.y, this.clientPosition.z, FreeCam.mc.player.rotationYaw, FreeCam.mc.player.rotationPitch);
        }
        this.removeFakePlayer();
        FreeCam.mc.player.motion = Vector3d.ZERO;
        super.onDisable();
        return false;
    }

    private void spawnFakePlayer() {
        this.fakePlayer = new RemoteClientPlayerEntity(FreeCam.mc.world, FreeCam.mc.player.getGameProfile());
        this.fakePlayer.copyLocationAndAnglesFrom(FreeCam.mc.player);
        FreeCam.mc.world.addEntity(21321313, this.fakePlayer);
    }

    private void removeFakePlayer() {
        FreeCam.mc.world.removeEntityFromWorld(21321313);
    }
}



