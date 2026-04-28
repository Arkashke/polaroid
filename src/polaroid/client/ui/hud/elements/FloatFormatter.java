package polaroid.client.ui.hud.elements;

public class FloatFormatter {
    public FloatFormatter(targetHudRender targetHudRender) {
    }

    public float format(float value) {
        float multiplier = (float)Math.pow(10.0, 1.0);
        return (float)Math.round(value * multiplier) / multiplier;
    }
}


