package polaroid.client.modules.api;

import polaroid.client.Polaroid;
import polaroid.client.modules.impl.render.ClientSounds;
import polaroid.client.modules.settings.Setting;
import polaroid.client.utils.client.ClientUtil;
import polaroid.client.utils.client.IMinecraft;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public abstract class Module implements IMinecraft {

    final String name;
    final Category category;
    final ServerCategory server;
    private final String description;
    boolean state;
    @Setter
    int bind;
    final List<Setting<?>> settings = new ObjectArrayList<>();

    final Animation animation = new Animation();
    
    public boolean expanded = false;

    public Module() {
        this.name = getClass().getAnnotation(ModuleSystem.class).name();
        this.category = getClass().getAnnotation(ModuleSystem.class).type();
        this.bind = getClass().getAnnotation(ModuleSystem.class).key();
        this.server = getClass().getAnnotation(ModuleSystem.class).server();
        this.description = this.getClass().getAnnotation(ModuleSystem.class).description();
    }

    public Module(String name) {
        this.name = name;
        this.category = Category.Combat;
        this.server = getServer();
        this.description = getClass().getAnnotation(ModuleSystem.class).description();
    }

    public void addSettings(Setting<?>... settings) {
        this.settings.addAll(List.of(settings));
    }

    public boolean onEnable() {
        animation.animate(1, 0.25f, Easings.CIRC_OUT);
        Polaroid.getInstance().getEventBus().register(this);
        return false;
    }

    public boolean onDisable() {
        animation.animate(0, 0.25f, Easings.CIRC_OUT);
        Polaroid.getInstance().getEventBus().unregister(this);
        return false;
    }


    public final void toggle() {
        setState(!state, false);
    }

    public final void setState(boolean newState, boolean config) {
        if (state == newState) {
            return;
        }

        state = newState;

        try {
            if (state) {
                onEnable();
            } else {
                onDisable();
            }
            if (!config) {
                ModuleRegistry functionRegistry = Polaroid.getInstance().getFunctionRegistry();
                ClientSounds clientSounds = functionRegistry.getClientSounds();

                if (clientSounds != null && clientSounds.isState()) {
                    String fileName = clientSounds.getFileName(state);
                    float volume = clientSounds.volume.get();
                    ClientUtil.playSound(fileName, volume, false);
                }
            }
        } catch (Exception e) {
            handleException(state ? "onEnable" : "onDisable", e);
        }

    }

    private void handleException(String methodName, Exception e) {
        if (mc.player != null) {
            print("[" + name + "] Произошла ошибка в методе " + TextFormatting.RED + methodName + TextFormatting.WHITE
                    + "() Предоставьте это сообщение разработчику: " + TextFormatting.GRAY + e.getMessage());
            e.printStackTrace();
        } else {
            System.out.println("[" + name + " Error" + methodName + "() Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


