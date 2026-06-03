package com.dyrnq.bdcm.service;

import com.dyrnq.bdcm.GrabProps;
import java.util.concurrent.*;
import lombok.Getter;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;

@Getter
@Component
public class ThreadPoolUtils {
    static final int DEFAULT_CAPACITY = 1000;
    static final int DEFAULT_THREAD_NUM = 100;

    @Inject
    GrabProps grabProps;
    // 单例模式，确保全局唯一线程池
    private ExecutorService executorService;

    @Init
    public void init() {

        // 初始化线程池（可以根据需要调整配置）

        int corePoolSize = 100;
        try {
            corePoolSize = Integer.parseInt(grabProps.getThreadPool().getCorePoolSize());
        } catch (Exception ignore) {
            corePoolSize = Runtime.getRuntime().availableProcessors();
        }
        int maxPoolSize = corePoolSize * 2;
        try {
            maxPoolSize = Integer.parseInt(grabProps.getThreadPool().getMaxPoolSize());
        } catch (Exception ignore) {

        }
        // 最大线程数
        long keepAliveTime = 60L; // 空闲线程存活时间
        int capacity = ThreadPoolUtils.DEFAULT_CAPACITY; // 任务队列容量
        if (grabProps.getThreadPool().getKeepAliveSeconds() > 0) {
            keepAliveTime = grabProps.getThreadPool().getKeepAliveSeconds();
        }
        if (grabProps.getThreadPool().getQueueCapacity() > 0) {
            capacity = grabProps.getThreadPool().getQueueCapacity();
        }

        TimeUnit unit = TimeUnit.SECONDS; // 时间单位
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(capacity); // 任务队列

        executorService = new ThreadPoolExecutor(
                corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, new ThreadPoolExecutor.AbortPolicy() // 拒绝策略
                );
        try {
            ((ThreadPoolExecutor) executorService).prestartAllCoreThreads();
        } catch (Exception ignore) {

        }
    }

    public int getCorePoolSize() {
        return ((ThreadPoolExecutor) executorService).getCorePoolSize();
    }

    public int getMaxPoolSize() {
        return ((ThreadPoolExecutor) executorService).getMaximumPoolSize();
    }

    public int getActiveCount() {
        return ((ThreadPoolExecutor) executorService).getActiveCount();
    }

    public long getTaskCount() {
        return ((ThreadPoolExecutor) executorService).getTaskCount();
    }

    public long getCompletedTaskCount() {
        return ((ThreadPoolExecutor) executorService).getCompletedTaskCount();
    }

    public int getQueueSize() {
        return ((ThreadPoolExecutor) executorService).getQueue().size();
    }

    public int getPoolSize() {
        return ((ThreadPoolExecutor) executorService).getPoolSize();
    }

    // 提交任务（Runnable）
    public void execute(Runnable task) {
        executorService.execute(task);
    }

    // 提交任务（Callable）
    public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(task);
    }

    // 关闭线程池
    public void shutdown() {
        executorService.shutdown();
    }

    // 强制关闭线程池
    public void shutdownNow() {
        executorService.shutdownNow();
    }

    // 等待线程池终止
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }
}
