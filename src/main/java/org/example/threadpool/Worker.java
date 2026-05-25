package org.example.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Worker implements Runnable {

    private final BlockingQueue<Runnable> taskQueue;
    private final int id;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final CustomThreadPool pool;


    private volatile boolean isRunning = true;
    private Thread thread;

    public Worker(int id, CustomThreadPool pool, long keepAliveTime, TimeUnit timeUnit, int queueSize) {
        this.id = id;
        this.taskQueue = new LinkedBlockingQueue<>(queueSize);
        this.pool = pool;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
    }

    public Thread getThread() {
        return thread;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public BlockingQueue<Runnable> getTaskQueue() {
        return taskQueue;
    }

    public int getId() {
        return id;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public void stop() {
        isRunning = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        while (isRunning && !pool.getIsShutdown()) {
            try {
                Runnable task = taskQueue.poll(keepAliveTime, timeUnit);

                if (task != null) {
                    if (pool.getIsShutdown()) {
                        break;
                    }

                    System.out.println("[Worker] " + thread.getName() + " executes task");
                    task.run();
                } else {
                    if (id > pool.getCorePoolSize()) {
                        System.out.println("[Worker] " + thread.getName() + " idle timeout, stopping");
                        break;
                    }
                }
            } catch (InterruptedException e) {
                if (pool.getIsShutdownNow()) {
                    break;
                }
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("[Worker] " + (thread != null ? thread.getName() : "Worker-" + id) + " terminated.");
        pool.removeWorker(this);
    }

    public boolean isIdle() {
        return taskQueue.isEmpty();
    }
}
