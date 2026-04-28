package polaroid.client.ui.hud.elements;

import polaroid.client.ui.hud.hudRender;
import polaroid.client.events.EventDisplay;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.ItemStack;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class armorStatusRender implements hudRender {

    @Override
    public void render(EventDisplay eventDisplay) {

        int posX = window.getScaledWidth() / 2 + 95;
        int posY = window.getScaledHeight() - (16 + 2);

        for (ItemStack itemStack : mc.player.getArmorInventoryList()) {
            if (itemStack.isEmpty()) continue;

            mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, posX, posY);
            mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, posX, posY, null);

            posX += 16 + 2;
        }
    }
}


