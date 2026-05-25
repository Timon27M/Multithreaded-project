package org.example.threadpool;

import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadFactory {
    private final String poolName;
    private final AtomicInteger counter = new AtomicInteger(1);

    public CustomThreadFactory(String poolName) {
        this.poolName = poolName;
    }

    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, poolName + "-worker-" + counter.getAndIncrement());

        System.out.println("[ThreadFactory] Creating new thread: " + thread.getName());

        return thread;
    }
}
