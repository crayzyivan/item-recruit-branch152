package com.item.framework.utils;

import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author : lh
 */
public class MDCThreadPoolExecutor extends ThreadPoolExecutor {

    /**
     * 构造方法，复用ThreadPoolExecutor的参数
     */
    public MDCThreadPoolExecutor(int corePoolSize,
                                 int maximumPoolSize,
                                 long keepAliveTime,
                                 TimeUnit unit,
                                 BlockingQueue<Runnable> workQueue,
                                 ThreadFactory threadFactory,
                                 RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public MDCThreadPoolExecutor(int corePoolSize,
                                 int maximumPoolSize,
                                 long keepAliveTime,
                                 TimeUnit unit,
                                 BlockingQueue<Runnable> workQueue,
                                 ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    /**
     * 重写execute方法，包装Runnable任务
     */
    @Override
    public void execute(Runnable command) {
        super.execute(wrap(command, MDC.getCopyOfContextMap()));
    }

    /**
     * 重写submit方法（Runnable类型）
     */
    @Override
    public Future submit(Runnable task) {
        return super.submit(wrap(task, MDC.getCopyOfContextMap()));
    }

    /**
     * 重写submit方法（Callable类型）
     */
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(wrap(task, MDC.getCopyOfContextMap()));
    }

    /**
     * 包装Runnable任务，传递MDC上下文
     */
    private Runnable wrap(Runnable runnable, Map<String, String> context) {
        return () -> {
            // 保存原始MDC上下文
            Map<String, String> originalContext = MDC.getCopyOfContextMap();
            try {
                // 设置当前任务的MDC上下文
                if (context != null) {
                    MDC.setContextMap(context);
                } else {
                    MDC.clear(); // 防止上下文污染
                }
                runnable.run();
            } finally {
                // 恢复原始MDC上下文（重要！防止线程复用导致的上下文泄漏）
                if (originalContext != null) {
                    MDC.setContextMap(originalContext);
                } else {
                    MDC.clear();
                }
            }
        };
    }

    /**
     * 包装Callable任务，传递MDC上下文
     */
    private <T> Callable<T> wrap(Callable<T> callable, Map<String, String> context) {
        return () -> {
            // 保存原始MDC上下文
            Map<String, String> originalContext = MDC.getCopyOfContextMap();
            try {
                // 设置当前任务的MDC上下文
                if (context != null) {
                    MDC.setContextMap(context);
                } else {
                    MDC.clear(); // 防止上下文污染
                }
                return callable.call();
            } finally {
                // 恢复原始MDC上下文（重要！防止线程复用导致的上下文泄漏）
                if (originalContext != null) {
                    MDC.setContextMap(originalContext);
                } else {
                    MDC.clear();
                }
            }
        };
    }
}
