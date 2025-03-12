package com.dyrnq.utils;

import java.util.concurrent.*;

public class ThreadPoolUtils {
    // 单例模式，确保全局唯一线程池
    private static final ExecutorService executorService;

    static {
        // 初始化线程池（可以根据需要调整配置）
        int corePoolSize = Runtime.getRuntime().availableProcessors(); // 核心线程数
        int maxPoolSize = corePoolSize * 2; // 最大线程数
        long keepAliveTime = 60L; // 空闲线程存活时间
        TimeUnit unit = TimeUnit.SECONDS; // 时间单位
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(100); // 任务队列

        executorService = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );
    }

    // 获取线程池实例
    public static ExecutorService getExecutorService() {
        return executorService;
    }

    // 提交任务（Runnable）
    public static void execute(Runnable task) {
        executorService.execute(task);
    }

    // 提交任务（Callable）
    public static <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(task);
    }

    // 关闭线程池
    public static void shutdown() {
        executorService.shutdown();
    }

    // 强制关闭线程池
    public static void shutdownNow() {
        executorService.shutdownNow();
    }

    // 等待线程池终止
    public static boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }
}