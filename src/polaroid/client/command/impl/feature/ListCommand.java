package polaroid.client.command.impl.feature;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;
import polaroid.client.command.Command;
import polaroid.client.command.Logger;
import polaroid.client.command.MultiNamedCommand;
import polaroid.client.command.Parameters;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListCommand implements Command, MultiNamedCommand {

    final List<Command> commands;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        logger.log(TextFormatting.RED + "advice" + TextFormatting.WHITE + " | " + TextFormatting.GRAY + "Помогает узнать как использовать команду");

        for (Command command : commands) {
            if (command == this) {
                continue;
            }
            logger.log(TextFormatting.RED + command.name() + TextFormatting.WHITE + " | " + TextFormatting.GRAY + command.description());
        }
    }

    @Override
    public String name() {
        return "list";
    }

    @Override
    public String description() {
        return "Выдает список всех команд";
    }

    @Override
    public List<String> aliases() {
        return List.of("", "help");
    }
}


