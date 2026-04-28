package polaroid.client.command.impl.feature;

import polaroid.client.Polaroid;
import polaroid.client.command.*;
import polaroid.client.command.impl.CommandException;
import polaroid.client.modules.api.Module;
import polaroid.client.utils.client.KeyStorage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BindCommand implements Command, CommandWithAdvice {

    final Prefix prefix;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElse("");

        switch (commandType) {
            case "add" -> addBindToFunction(parameters, logger);
            case "remove" -> removeBindFromFunction(parameters, logger);
            case "clear" -> clearAllBindings(logger);
            case "list" -> listBoundKeys(logger);
            default -> {
                // Упрощенный синтаксис: .bind name hotkey
                String functionName = commandType;
                String keyName = parameters.asString(1)
                        .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите кнопку!"));
                
                Module function = null;
                for (Module func : Polaroid.getInstance().getFunctionRegistry().getFunctions()) {
                    if (func.getName().toLowerCase(Locale.ROOT).equals(functionName.toLowerCase(Locale.ROOT))) {
                        function = func;
                        break;
                    }
                }

                Integer key = KeyStorage.getKey(keyName.toUpperCase());

                if (function == null) {
                    throw new CommandException(TextFormatting.RED + "Функция " + functionName + " не была найдена");
                }

                if (key == null) {
                    throw new CommandException(TextFormatting.RED + "Клавиша " + keyName + " не была найдена");
                }

                function.setBind(key);
                logger.log(TextFormatting.GREEN + "Бинд " + TextFormatting.RED
                        + keyName.toUpperCase() + TextFormatting.GREEN
                        + " был установлен для функции " + TextFormatting.RED + functionName);
            }
        }
    }

    @Override
    public String name() {
        return "bind";
    }

    @Override
    public String description() {
        return "Позволяет забиндить функцию на определенную клавишу";
    }

    @Override
    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();
        return List.of(commandPrefix + "bind add <function> <key> - Добавить новый бинд",
                commandPrefix + "bind remove <function> <key> - Удалить бинд",
                commandPrefix + "bind list - Получить список биндов",
                commandPrefix + "bind clear - Очистить список биндов",
                "Пример: " + TextFormatting.RED + commandPrefix + "bind add Aura R"
        );
    }

    private void addBindToFunction(Parameters parameters, Logger logger) {
        String functionName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите название функции!"));
        String keyName = parameters.asString(2)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите кнопку!"));

        Module function = null;

        for (Module func : Polaroid.getInstance().getFunctionRegistry().getFunctions()) {
            if (func.getName().toLowerCase(Locale.ROOT).equals(functionName.toLowerCase(Locale.ROOT))) {
                function = func;
                break;
            }
        }

        Integer key = KeyStorage.getKey(keyName.toUpperCase());

        if (function == null) {
            logger.log(TextFormatting.RED + "Функция " + functionName + " не была найдена");
            return;
        }

        if (key == null) {
            logger.log(TextFormatting.RED + "Клавиша " + keyName + " не была найдена");
            return;
        }

        function.setBind(key);
        logger.log(TextFormatting.GREEN + "Бинд " + TextFormatting.RED
                + keyName.toUpperCase() + TextFormatting.GREEN
                + " был установлен для функции " + TextFormatting.RED + functionName);
    }

    private void removeBindFromFunction(Parameters parameters, Logger logger) {
        String functionName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите название функции!"));
        String keyName = parameters.asString(2)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите кнопку!"));

        Polaroid.getInstance().getFunctionRegistry().getFunctions().stream()
                .filter(f -> f.getName().equalsIgnoreCase(functionName))
                .forEach(f -> {
                    f.setBind(0);
                    logger.log(TextFormatting.GREEN + "Клавиша " + TextFormatting.RED + keyName.toUpperCase()
                            + TextFormatting.GREEN + " была отвязана от функции " + TextFormatting.RED + f.getName());
                });
    }

    private void clearAllBindings(Logger logger) {
        Polaroid.getInstance().getFunctionRegistry().getFunctions().forEach(function -> function.setBind(0));
        logger.log(TextFormatting.GREEN + "Все клавиши были отвязаны от модулей");
    }

    private void listBoundKeys(Logger logger) {
        logger.log(TextFormatting.GRAY + "Список всех модулей с привязанными клавишами:");

        Polaroid.getInstance().getFunctionRegistry().getFunctions().stream()
                .filter(f -> f.getBind() != 0)
                .map(f -> {
                    String keyName = GLFW.glfwGetKeyName(f.getBind(), -1);
                    keyName = keyName != null ? keyName : "";
                    return String.format("%s [%s%s%s]", f.getName(), TextFormatting.GRAY, keyName, TextFormatting.WHITE);
                })
                .forEach(logger::log);
    }

}


