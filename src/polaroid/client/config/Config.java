package polaroid.client.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import polaroid.client.Polaroid;
import polaroid.client.modules.settings.Setting;
import polaroid.client.modules.settings.impl.*;
import polaroid.client.ui.styles.Style;
import polaroid.client.utils.client.IMinecraft;
import polaroid.client.utils.drag.DragManager;
import polaroid.client.utils.drag.Dragging;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class Config implements IMinecraft {
    private final File file;
    private final String name;
    public static String currentConfig = "autocfg";

    Polaroid instance = Polaroid.getInstance();

    public Config(String name) {
        this.name = name;
        this.file = new File(new File(Minecraft.getInstance().gameDir, "\\polaroid\\configs"), name + ".cfg");
    }

    public void loadConfig(JsonObject jsonObject) {
        if (jsonObject == null) {
            return;
        }

        if (jsonObject.has("functions")) {
            loadFunctionSettings(jsonObject.getAsJsonObject("functions"));
        }

        if (jsonObject.has("styles")) {
            loadStyleSettings(jsonObject.getAsJsonObject("styles"));
        }

        if (jsonObject.has("draggables")) {
            loadDraggables(jsonObject.getAsJsonArray("draggables"));
        }
    }

    private void loadDraggables(JsonArray draggablesArray) {
        for (JsonElement element : draggablesArray) {
            JsonObject draggableObject = element.getAsJsonObject();
            String name = draggableObject.get("name").getAsString();
            Dragging dragging = DragManager.draggables.get(name);
            if (dragging != null) {
                dragging.setX(draggableObject.get("x").getAsInt());
                dragging.setY(draggableObject.get("y").getAsInt());
            }
        }
    }

    private void loadStyleSettings(JsonObject stylesObject) {
        for (Map.Entry<String, JsonElement> entry : stylesObject.entrySet()) {
            String styleName = entry.getKey();
            JsonObject styleObject = entry.getValue().getAsJsonObject();
            Style style = findStyleByName(styleName);
            if (style != null && styleObject.has("selected")) {
                boolean isSelected = styleObject.get("selected").getAsBoolean();
                if (isSelected) {
                    instance.getStyleManager().setCurrentStyle(style);
                }
            }
        }
    }

    private Style findStyleByName(String styleName) {
        for (Style style : instance.getStyleManager().getStyleList()) {
            if (style.getStyleName().equalsIgnoreCase(styleName)) {
                return style;
            }
        }
        return null;
    }

    private void loadFunctionSettings(JsonObject functionsObject) {
        instance.getFunctionRegistry().getFunctions().forEach(f -> {
            JsonObject moduleObject = functionsObject.getAsJsonObject(f.getName().toLowerCase());
            if (moduleObject == null) {
                return;
            }

            f.setState(false, true);
            loadSettingFromJson(moduleObject, "bind", value -> f.setBind(value.getAsInt()));
            loadSettingFromJson(moduleObject, "state", value -> f.setState(value.getAsBoolean(), true));
            f.getSettings().forEach(setting -> loadIndividualSetting(moduleObject, setting));
        });
    }

    private void loadIndividualSetting(JsonObject moduleObject, Setting<?> setting) {
        JsonElement settingElement = moduleObject.get(setting.getName());

        if (settingElement == null || settingElement.isJsonNull()) {
            return;
        }

        if (setting instanceof SliderSetting) {
            ((SliderSetting) setting).set(settingElement.getAsFloat());
        } else if (setting instanceof BooleanSetting) {
            ((BooleanSetting) setting).set(settingElement.getAsBoolean());
        } else if (setting instanceof ColorSetting) {
            ((ColorSetting) setting).set(settingElement.getAsInt());
        } else if (setting instanceof ModeSetting) {
            ((ModeSetting) setting).set(settingElement.getAsString());
        } else if (setting instanceof BindSetting) {
            ((BindSetting) setting).set(settingElement.getAsInt());
        } else if (setting instanceof StringSetting) {
            ((StringSetting) setting).set(settingElement.getAsString());
        } else if (setting instanceof ModeListSetting) {
            loadModeListSetting((ModeListSetting) setting, moduleObject);
        }
    }

    private void loadModeListSetting(ModeListSetting setting, JsonObject moduleObject) {
        JsonObject elements = moduleObject.getAsJsonObject(setting.getName());
        setting.get().forEach(option -> {
            JsonElement optionElement = elements.get(option.getName());
            if (optionElement != null && !optionElement.isJsonNull()) {
                option.set(optionElement.getAsBoolean());
            }
        });
    }

    private void loadSettingFromJson(JsonObject jsonObject, String key, Consumer<JsonElement> consumer) {
        JsonElement element = jsonObject.get(key);
        if (element != null && !element.isJsonNull()) {
            consumer.accept(element);
        }
    }

    public JsonElement saveConfig() {
        JsonObject functionsObject = new JsonObject();
        JsonObject stylesObject = new JsonObject();
        JsonArray draggablesArray = new JsonArray();

        saveFunctionSettings(functionsObject);
        saveStyleSettings(stylesObject);
        saveDraggables(draggablesArray);

        JsonObject newObject = new JsonObject();
        newObject.add("functions", functionsObject);
        newObject.add("styles", stylesObject);
        newObject.add("draggables", draggablesArray);

        return newObject;
    }

    private void saveFunctionSettings(JsonObject functionsObject) {
        instance.getFunctionRegistry().getFunctions().forEach(module -> {
            JsonObject moduleObject = new JsonObject();

            moduleObject.addProperty("bind", module.getBind());
            moduleObject.addProperty("state", module.isState());

            module.getSettings().forEach(setting -> saveIndividualSetting(moduleObject, setting));

            functionsObject.add(module.getName().toLowerCase(), moduleObject);
        });
    }

    private void saveIndividualSetting(JsonObject moduleObject, Setting<?> setting) {
        if (setting instanceof BooleanSetting) {
            moduleObject.addProperty(setting.getName(), ((BooleanSetting) setting).get());
        } else if (setting instanceof SliderSetting) {
            moduleObject.addProperty(setting.getName(), ((SliderSetting) setting).get());
        } else if (setting instanceof ModeSetting) {
            moduleObject.addProperty(setting.getName(), ((ModeSetting) setting).get());
        } else if (setting instanceof ColorSetting) {
            moduleObject.addProperty(setting.getName(), ((ColorSetting) setting).get());
        } else if (setting instanceof BindSetting) {
            moduleObject.addProperty(setting.getName(), ((BindSetting) setting).get());
        } else if (setting instanceof StringSetting) {
            moduleObject.addProperty(setting.getName(), ((StringSetting) setting).get());
        } else if (setting instanceof ModeListSetting) {
            saveModeListSetting(moduleObject, (ModeListSetting) setting);
        }
    }

    private void saveModeListSetting(JsonObject moduleObject, ModeListSetting setting) {
        JsonObject elements = new JsonObject();
        setting.get().forEach(option -> elements.addProperty(option.getName(), option.get()));
        moduleObject.add(setting.getName(), elements);
    }

    private void saveStyleSettings(JsonObject stylesObject) {
        for (Style style : instance.getStyleManager().getStyleList()) {
            JsonObject styleObject = new JsonObject();
            styleObject.addProperty("selected", instance.getStyleManager().getCurrentStyle() == style);
            stylesObject.add(style.getStyleName(), styleObject);
        }
    }

    private void saveDraggables(JsonArray draggablesArray) {
        for (Map.Entry<String, Dragging> entry : DragManager.draggables.entrySet()) {
            JsonObject draggableObject = new JsonObject();
            draggableObject.addProperty("name", entry.getKey());
            draggableObject.addProperty("x", entry.getValue().getX());
            draggableObject.addProperty("y", entry.getValue().getY());
            draggablesArray.add(draggableObject);
        }
    }
}

