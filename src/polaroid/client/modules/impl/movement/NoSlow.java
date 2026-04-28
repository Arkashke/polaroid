package polaroid.client.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.NoSlowEvent;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.ModeSetting;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;

@ModuleSystem(name = "NoSlow", type = Category.Movement, server = ServerCategory.NO, description = "Убирает замедление")
public class NoSlow extends Module {
    
    private int ticks;
    private final ModeSetting mode = new ModeSetting("Режим", "Grim", "Grim", "Spooky", "HolyWorld");

    public NoSlow() {
        addSettings(mode);
    }

    @Subscribe
    public void onSlowDown(NoSlowEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        if (!mc.player.isHandActive()) {
            ticks = 0;
        } else {
            if (mode.is("Spooky") || mode.is("HolyWorld")) {
                ticks++;
            }

            UseAction mainAction = mc.player.getHeldItemMainhand().getUseAction();
            UseAction offAction = mc.player.getHeldItemOffhand().getUseAction();
            Hand activeHand = mc.player.getActiveHand();

            if ((mainAction != UseAction.BLOCK && offAction != UseAction.EAT || activeHand != Hand.MAIN_HAND) 
                && mc.player.isHandActive()) {
                
                mc.player.setSprinting(true);
                
                if (activeHand == Hand.MAIN_HAND && !mode.is("Spooky")) {
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                    event.cancel();
                } else {
                    if (!mode.is("Spooky") && !mode.is("HolyWorld")) {
                        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                    }
                    if (ticks >= 2 || mode.is("Grim") || mode.is("HolyWorld")) {
                        event.cancel();
                        ticks = 0;
                    }
                }
            }
        }
    }
}


