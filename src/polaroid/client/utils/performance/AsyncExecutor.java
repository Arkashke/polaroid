package polaroid.client.utils.performance;

import polaroid.client.Polaroid;

import java.util.concurrent.*;

/**
 * Асинхронный исполнитель для тяжелых операций
 */
public class AsyncExecutor {
    
    private static final AsyncExecutor INSTANCE = new AsyncExecutor();
    
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;
    
    private AsyncExecutor() {
        // Создаем пул потоков с ограниченным размером
        this.executor = new ThreadPoolExecutor(
            2, // минимум потоков
            4, // максимум потоков
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadFactory() {
                private int counter = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, Polaroid.CLIENT_NAME + "-Async-" + counter++);
                    thread.setDaemon(true);
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // Если очередь заполнена, выполняем в текущем потоке
        );
        
        this.scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread thread = new Thread(r, Polaroid.CLIENT_NAME + "-Scheduler");
            thread.setDaemon(true);
            return thread;
        });
    }
    
    public static AsyncExecutor getInstance() {
        return INSTANCE;
    }
    
    /**
     * Выполняет задачу асинхронно
     */
    public <T> CompletableFuture<T> execute(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor);
    }
    
    /**
     * Выполняет задачу асинхронно без возвращаемого значения
     */
    public CompletableFuture<Void> execute(Runnable task) {
        return CompletableFuture.runAsync(task, executor);
    }
    
    /**
     * Выполняет задачу с задержкой
     */
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return scheduler.schedule(task, delay, unit);
    }
    
    /**
     * Выполняет задачу периодически
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
    }
    
    /**
     * Останавливает все задачи
     */
    public void shutdown() {
        executor.shutdown();
        scheduler.shutdown();
    }
}


