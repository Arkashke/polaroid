package polaroid.client.modules.impl.combat;

import com.google.common.eventbus.Subscribe;
import polaroid.client.command.friends.FriendStorage;
import polaroid.client.events.AttackEvent;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import net.minecraft.entity.player.PlayerEntity;

@ModuleSystem(name = "NoFriendDamage", type = Category.Combat, server = ServerCategory.NO, description = "Предотвращает атаку друзей")
public class NoFriendDamage extends Module {

    @Subscribe
    public void onAttack(AttackEvent event) {
        if (event.entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.entity;
            if (FriendStorage.isFriend(player.getName().getString())) {
                event.cancel();
            }
        }
    }
}


