package polaroid.client.ui.hud;

import polaroid.client.events.EventUpdate;
import polaroid.client.utils.client.IMinecraft;

public interface hudUpdater extends IMinecraft {

    void update(EventUpdate e);
}


