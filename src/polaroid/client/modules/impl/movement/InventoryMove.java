package polaroid.client.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventPacket;
import polaroid.client.events.EventUpdate;
import polaroid.client.events.InventoryCloseEvent;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.utils.math.StopWatch;
import polaroid.client.utils.player.MoveUtils;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClickWindowPacket;

import java.util.ArrayList;
import java.util.List;

@ModuleSystem(name = "GuiMove", type = Category.Movement, server = ServerCategory.NO, description = "Позволяет бегать с открытым инвентарём")
public class InventoryMove extends Module {

    private static final List<IPacket<?>> packet = new ArrayList<>();
    private final BooleanSetting ft = new BooleanSetting("Обход FunTime", false);
    private final BooleanSetting hw = new BooleanSetting("Обход HollyWorld", false);
    private static final StopWatch wait = new StopWatch();
    private static boolean spookySwapping = false;
    
    public InventoryMove() {
        addSettings(ft, hw);
    }
    
    public boolean isFunTimeBypass() {
        return ft.get();
    }
    
    // Метод для активации SpookyTime свапа с блокировкой движения
    public static void resetTimer() {
        spookySwapping = true;
        wait.reset();
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player != null) {

            final KeyBinding[] pressedKeys = {mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack,
                    mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump,
                    mc.gameSettings.keyBindSprint};
            
            // Блокируем движение при SpookyTime свапе на 400мс
            if (spookySwapping) {
                if (!wait.isReached(400)) {
                    for (KeyBinding keyBinding : pressedKeys) {
                        keyBinding.setPressed(false);
                    }
                    return;
                } else {
                    spookySwapping = false;
                }
            }
            
            if (ft.get()) {
                if (!wait.isReached(400)) {
                    for (KeyBinding keyBinding : pressedKeys) {
                        keyBinding.setPressed(false);
                    }
                    return;
                }
            }

            if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof EditSignScreen) {
                return;
            }

            updateKeyBindingState(pressedKeys);

        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (ft.get()) {
            if (e.getPacket() instanceof CClickWindowPacket p && MoveUtils.isMoving()) {
                if (mc.currentScreen instanceof InventoryScreen) {
                    packet.add(p);
                    e.cancel();
                }
            }
        }
    }

    @Subscribe
    public void onClose(InventoryCloseEvent e) {
        if (ft.get()) {
            if (mc.currentScreen instanceof InventoryScreen && !packet.isEmpty() && MoveUtils.isMoving()) {
                new Thread(() -> {
                    wait.reset();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    for (IPacket p : packet) {
                        mc.player.connection.sendPacketWithoutEvent(p);
                    }
                    packet.clear();
                }).start();
                e.cancel();
            }
        }
    }

    private void updateKeyBindingState(KeyBinding[] keyBindings) {
        for (KeyBinding keyBinding : keyBindings) {
            boolean isKeyPressed = InputMappings.isKeyDown(mc.getMainWindow().getHandle(), keyBinding.getDefault().getKeyCode());
            keyBinding.setPressed(isKeyPressed);
        }
    }
}


