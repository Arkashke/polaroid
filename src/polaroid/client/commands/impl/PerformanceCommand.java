package polaroid.client.commands.impl;

import polaroid.client.command.Command;
import polaroid.client.command.Logger;
import polaroid.client.command.Parameters;
import polaroid.client.utils.performance.PerformanceProfiler;
import polaroid.client.utils.performance.RenderCache;
import polaroid.client.utils.performance.ModuleThrottler;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PerformanceCommand implements Command {

    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String subCommand = parameters.asString(0).orElse("");
        
        if (subCommand.isEmpty()) {
            logger.log("§7Использование:");
            logger.log("§7.performance profile §f- Включить/выключить профилирование");
            logger.log("§7.performance stats §f- Показать статистику производительности");
            logger.log("§7.performance clear §f- Очистить все кэши");
            logger.log("§7.performance reset §f- Сбросить throttling");
            return;
        }

        switch (subCommand.toLowerCase()) {
            case "profile":
                toggleProfiling();
                break;
                
            case "stats":
                showStats();
                break;
                
            case "clear":
                clearCaches();
                break;
                
            case "reset":
                resetThrottling();
                break;
                
            default:
                logger.log("§cНеизвестная команда. Используйте .performance для справки");
                break;
        }
    }

    private void toggleProfiling() {
        PerformanceProfiler profiler = PerformanceProfiler.getInstance();
        boolean newState = !profiler.isEnabled();
        profiler.setEnabled(newState);
        
        if (newState) {
            logger.log("§aПрофилирование включено");
            logger.log("§7Используйте §f.performance stats §7для просмотра результатов");
        } else {
            logger.log("§cПрофилирование выключено");
        }
    }

    private void showStats() {
        PerformanceProfiler profiler = PerformanceProfiler.getInstance();
        
        if (!profiler.isEnabled()) {
            logger.log("§cПрофилирование выключено. Используйте §f.performance profile §cдля включения");
            return;
        }

        Map<String, PerformanceProfiler.ProfileData> data = profiler.getProfileData();
        
        if (data.isEmpty()) {
            logger.log("§7Нет данных профилирования");
            return;
        }

        logger.log("§6=== Статистика производительности ===");
        
        data.entrySet().stream()
            .sorted((e1, e2) -> Double.compare(e2.getValue().getTotalTimeMs(), e1.getValue().getTotalTimeMs()))
            .limit(10)
            .forEach(entry -> {
                String name = entry.getKey();
                PerformanceProfiler.ProfileData stats = entry.getValue();
                
                logger.log(String.format("§7%s: §fСредн: %.3fms §7| §fМакс: %.3fms §7| §fВызовов: %d",
                    name,
                    stats.getAverageTimeMs(),
                    stats.getMaxTimeMs(),
                    stats.callCount
                ));
            });
            
        logger.log("§6===================================");
    }

    private void clearCaches() {
        RenderCache.getInstance().invalidateAll();
        PerformanceProfiler.getInstance().reset();
        
        logger.log("§aВсе кэши очищены");
        logger.log("§7Данные профилирования сброшены");
    }

    private void resetThrottling() {
        ModuleThrottler.getInstance().resetAll();
        logger.log("§aThrottling сброшен для всех модулей");
    }

    @Override
    public String name() {
        return "performance";
    }

    @Override
    public String description() {
        return "Управление производительностью клиента";
    }
}


