package polaroid.client.utils.rotation;

import polaroid.client.utils.rotation.impl.FunTimeRotation;
import polaroid.client.utils.rotation.impl.SpookyTimeRotation;
import polaroid.client.utils.rotation.impl.SpookyTimeDuelsRotation;
import polaroid.client.utils.rotation.impl.ReallyWorldRotation;
import polaroid.client.utils.rotation.impl.HollyWorldRotation;
import polaroid.client.utils.rotation.impl.SimpleRotation;
import polaroid.client.utils.rotation.impl.IntaveRotation;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class RotationManager {
    
    private final Map<String, RotationHandler> rotations = new HashMap<>();
    
    public RotationManager() {
        registerRotation(new FunTimeRotation());
        registerRotation(new SpookyTimeRotation());
        registerRotation(new SpookyTimeDuelsRotation());
        registerRotation(new ReallyWorldRotation());
        registerRotation(new HollyWorldRotation());
        registerRotation(new SimpleRotation());
        registerRotation(new IntaveRotation());
    }
    
    private void registerRotation(RotationHandler handler) {
        rotations.put(handler.getName(), handler);
    }
    
    public RotationHandler getRotation(String name) {
        return rotations.getOrDefault(name, rotations.get("FunTime"));
    }
}


