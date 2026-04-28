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

import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UnbindCommand implements Command, CommandWithAdvice {

    final Prefix prefix;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String keyName = parameters.asString(0)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите кнопку!"));

        Integer key = KeyStorage.getKey(keyName.toUpperCase());

        if (key == null) {
            throw new CommandException(TextFormatting.RED + "Клавиша " + keyName + " не была найдена");
        }

        boolean found = false;
        for (Module function : Polaroid.getInstance().getFunctionRegistry().getFunctions()) {
            if (function.getBind() == key) {
                function.setBind(0);
                logger.log(TextFormatting.GREEN + "Клавиша " + TextFormatting.RED + keyName.toUpperCase()
                        + TextFormatting.GREEN + " была отвязана от функции " + TextFormatting.RED + function.getName());
                found = true;
            }
        }

        if (!found) {
            logger.log(TextFormatting.RED + "Клавиша " + keyName.toUpperCase() + " не привязана ни к одной функции");
        }
    }

    @Override
    public String name() {
        return "unbind";
    }

    @Override
    public String description() {
        return "Позволяет отвязать клавишу от всех функций";
    }

    @Override
    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();
        return List.of(commandPrefix + "unbind <key> - Отвязать клавишу от всех функций",
                "Пример: " + TextFormatting.RED + commandPrefix + "unbind R"
        );
    }
}


