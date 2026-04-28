package polaroid.client.modules.impl.player;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventKey;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BindSetting;
import polaroid.client.utils.player.PlayerUtils;
import net.minecraft.entity.player.PlayerEntity;
import polaroid.client.command.friends.FriendStorage;
import polaroid.client.modules.api.ModuleSystem;

@ModuleSystem(name = "ClickFriend", type = Category.Player, server = ServerCategory.NO, description = "Позволяет добавить игрока в друзья на определённую кнопку")
public class ClickFriend extends Module {
    final BindSetting throwKey = new BindSetting("Кнопка", -98);
    public ClickFriend() {
        addSettings(throwKey);
    }
    @Subscribe
    public void onKey(EventKey e) {
        if (e.getKey() == throwKey.get() && mc.pointedEntity instanceof PlayerEntity) {

            if (mc.player == null || mc.pointedEntity == null) {
                return;
            }

            String playerName = mc.pointedEntity.getName().getString();

            if (!PlayerUtils.isNameValid(playerName)) {
                print("Невозможно добавить бота в список друзей!");
                return;
            }

            if (FriendStorage.isFriend(playerName)) {
                FriendStorage.remove(playerName);
                printStatus(playerName, true);
            } else {
                FriendStorage.add(playerName);
                printStatus(playerName, false);
            }
        }
    }

    void printStatus(String name, boolean remove) {
        if (remove) print(name + " удалён из друзей");
        else print(name + " добавлен в друзья");
    }
}


