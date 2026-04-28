package polaroid.client.ui.hud;

import polaroid.client.events.EventDisplay;
import polaroid.client.utils.client.IMinecraft;

public interface hudRender extends IMinecraft {
    void render(EventDisplay eventDisplay);
}


