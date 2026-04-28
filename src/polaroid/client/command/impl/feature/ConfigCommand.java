package polaroid.client.command.impl.feature;

import polaroid.client.Polaroid;
import polaroid.client.command.*;
import polaroid.client.command.impl.CommandException;
import polaroid.client.config.Config;
import polaroid.client.config.ConfigStorage;
import polaroid.client.modules.settings.impl.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigCommand implements Command, CommandWithAdvice, MultiNamedCommand {

    final ConfigStorage configStorage;
    final Prefix prefix;
    final Logger logger;


    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElse("");

        switch (commandType) {
            case "load", "create" -> loadOrCreateConfig(parameters, commandType);
            case "save" -> saveConfig(parameters);
            case "list" -> configList();
            case "dir" -> getDirectory();
            case "reset" -> resetConfig();
            default ->
                    throw new CommandException(TextFormatting.RED + "Укажите тип команды:" + TextFormatting.GRAY + " load, save, create, list, dir, reset");
        }
    }

    // ... (
    @Override
    public String name() {
        return "config";
    }

    @Override
    public String description() {
        return "Позволяет взаимодействовать с конфигами в чите";
    }

    @Override
    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();

        return List.of(commandPrefix + name() + " load <config> - Загрузить конфиг",
                commandPrefix + name() + " create <config> - Создать и сразу сохранить конфиг",
                commandPrefix + name() + " save <config> - Сохранить конфиг",
                commandPrefix + name() + " list - Получить список конфигов",
                commandPrefix + name() + " dir - Открыть папку с конфигами",
                commandPrefix + name() + " reset - Сбросить все настройки и бинды модулей",
                "Пример: " + TextFormatting.RED + commandPrefix + "cfg create main",
                "Пример: " + TextFormatting.RED + commandPrefix + "cfg load main"

        );
    }

    @Override
    public List<String> aliases() {
        return List.of("cfg");
    }

    private void loadOrCreateConfig(Parameters parameters, String mode) {
        String configName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите название конфига!"));

        File file = new File(configStorage.CONFIG_DIR, configName + ".cfg");

        if ("create".equalsIgnoreCase(mode)) {
            // .cfg create <name> — просто сохраняем текущие настройки в новый/существующий конфиг
            configStorage.saveConfiguration(configName);
            logger.log(TextFormatting.GREEN + "Конфигурация " + TextFormatting.RED + configName + TextFormatting.GREEN + " создана и сохранена!");
            return;
        }

        if (file.exists()) {
            configStorage.loadConfiguration(configName);
            logger.log(TextFormatting.GREEN + "Конфигурация " + TextFormatting.RED + configName + TextFormatting.GREEN + " загружена!");
        } else {
            logger.log(TextFormatting.RED + "Конфигурация " + TextFormatting.GRAY + configName + TextFormatting.RED + " не найдена!");
        }
    }

    private void saveConfig(Parameters parameters) {
        String configName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите название конфига!"));

        configStorage.saveConfiguration(configName);
        logger.log(TextFormatting.GREEN + "Конфигурация " + TextFormatting.RED + configName + TextFormatting.GREEN + " сохранена!");

    }

    private void configList() {
        if (configStorage.isEmpty()) {
            logger.log(TextFormatting.RED + "Список конфигураций пустой");
            return;
        }
        logger.log(TextFormatting.GRAY + "Список конфигов:");

        for (Config config : configStorage.getConfigs()) {
            logger.log(TextFormatting.GRAY + config.getName());
        }
    }

    private void getDirectory() {
        try {
            Runtime.getRuntime().exec("explorer " + configStorage.CONFIG_DIR.getAbsolutePath());
        } catch (IOException e) {
            logger.log(TextFormatting.RED + "Папка с конфигурациями не найдена!" + e.getMessage());
        }
    }
    
    private void resetConfig() {
        Polaroid.getInstance().getFunctionRegistry().getFunctions().forEach(module -> {
            // Выключаем модуль
            if (module.isState()) {
                module.setState(false, true);
            }
            
            // Сбрасываем бинд
            module.setBind(0);
            
            // Сбрасываем настройки к значениям по умолчанию
            module.getSettings().forEach(setting -> {
                if (setting instanceof BooleanSetting) {
                    // Для BooleanSetting сбрасываем на false
                    ((BooleanSetting) setting).set(false);
                } else if (setting instanceof SliderSetting) {
                    // Для SliderSetting оставляем текущее значение (можно изменить логику)
                } else if (setting instanceof ModeSetting) {
                    // Для ModeSetting сбрасываем на первый вариант
                    ModeSetting modeSetting = (ModeSetting) setting;
                    if (modeSetting.strings.length > 0) {
                        modeSetting.set(modeSetting.strings[0]);
                    }
                } else if (setting instanceof ModeListSetting) {
                    // Для ModeListSetting выключаем все опции
                    ModeListSetting modeListSetting = (ModeListSetting) setting;
                    modeListSetting.get().forEach(option -> {
                        if (option instanceof BooleanSetting) {
                            ((BooleanSetting) option).set(false);
                        }
                    });
                } else if (setting instanceof BindSetting) {
                    // Для BindSetting сбрасываем на -1
                    ((BindSetting) setting).set(-1);
                } else if (setting instanceof ColorSetting) {
                    // Для ColorSetting сбрасываем на белый цвет
                    ((ColorSetting) setting).set(-1);
                }
            });
        });
        
        logger.log(TextFormatting.GREEN + "Все настройки сброшены к значениям по умолчанию!");
    }
}


