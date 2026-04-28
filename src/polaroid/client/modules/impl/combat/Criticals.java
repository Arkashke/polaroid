package polaroid.client.modules.impl.combat;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventPacket;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.ModeSetting;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CUseEntityPacket;

@ModuleSystem(name = "Criticals", type = Category.Combat, server = ServerCategory.NO, description = "Позволяет наносить критические удары стоя на земле")
public class Criticals extends Module {
    
    private final ModeSetting mode = new ModeSetting("Режим", "Packet", "Packet");

    public Criticals() {
        addSettings(mode);
    }
    
    public boolean canCritical() {
        return isState() && 
               mc.player.isOnGround() && 
               !mc.player.isInWater() && 
               !mc.player.isInLava() && 
               !mc.player.isOnLadder();
    }
    
    @Subscribe
    private void onPacket(EventPacket event) {
        if (event.isSend()) {
            IPacket<?> packet = event.getPacket();
            
            if (packet instanceof CUseEntityPacket) {
                CUseEntityPacket useEntityPacket = (CUseEntityPacket) packet;
                
                // Проверяем, что это атака
                if (useEntityPacket.getAction() == CUseEntityPacket.Action.ATTACK) {
                    if (canCritical() && mode.is("Packet")) {
                        sendCriticalPackets();
                    }
                }
            }
        }
    }
    
    private void sendCriticalPackets() {
        double x = mc.player.getPosX();
        double y = mc.player.getPosY();
        double z = mc.player.getPosZ();
        
        // Отправляем пакеты для критического удара
        mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y + 0.0625, z, false));
        mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y, z, false));
    }
}


