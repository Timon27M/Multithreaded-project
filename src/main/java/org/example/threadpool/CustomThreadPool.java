package org.example.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadPool implements CustomExecutor {
    private final CustomThreadFactory customThreadFactory;
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);
    private final AtomicInteger workerIdCounter = new AtomicInteger(1);

    private final int corePoolSize;
    private final int maxPoolSize;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final int queueSize;
    private final int minSpareThreads;

    private final List<Worker> workers;

    private volatile boolean isShutdown = false;
    private volatile boolean isShutdownNow = false;

    private final RejectedExecutionHandler rejectedHandler;

    public CustomThreadPool(String poolName, int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit timeUnit, int queueSize, int minSpareThreads) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.queueSize = queueSize;
        this.minSpareThreads = minSpareThreads;

        this.workers = new ArrayList<Worker>();
        this.customThreadFactory = new CustomThreadFactory(poolName);
        this.rejectedHandler = new DefaultRejectedHandler();

        for (int i = 0; i < corePoolSize; i++) {
            addWorker();
        }
    }

    public boolean getIsShutdown() {
        return isShutdown;
    }

    public List<Worker> getWorkers() {
        return workers;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public boolean getIsShutdownNow() {
        return isShutdownNow;
    }


    @Override
    public void execute(Runnable command) {
        if (isShutdown || isShutdownNow) {
            rejectedHandler.rejectedExecution(command, this);
            return;
        }

        long idleCount = workers.stream().filter(Worker::isIdle).count();
        if (idleCount < minSpareThreads && workers.size() < maxPoolSize) {
            addWorker();
        }

        Worker worker = workers.get(roundRobinIndex.getAndIncrement() % workers.size());

        boolean isAdded = worker.getTaskQueue().offer(command);

        if (isAdded) {
            System.out.println("[Pool] Task accepted into queue of Worker-" + worker.getId());
        } else {
            if (workers.size() < maxPoolSize) {
                addWorker();
                Worker newWorker = workers.get(workers.size() - 1);

                newWorker.getTaskQueue().offer(command);

                System.out.println("[Pool] Task accepted into queue of Worker-" + newWorker.getId());
            } else {
                rejectedHandler.rejectedExecution(command, this);
            }
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        if (isShutdown || isShutdownNow) {
            throw new RuntimeException("Pool is shutdown");
        }
        FutureTask<T> futureTask = new FutureTask<T>(callable);
        execute(futureTask);


        return futureTask;
    }

    @Override
    public void shutdown() {
        isShutdown = true;
        System.out.println("[Pool] Shutdown initiated. No new tasks accepted.");
    }

    @Override
    public void shutdownNow() {
        isShutdownNow = true;
        isShutdown = true;
        System.out.println("[Pool] Shutdown NOW initiated. Interrupting all workers.");

        for (Worker worker : workers) {
            worker.stop();
        }
    }

    public void addWorker() {
        int id = workerIdCounter.getAndIncrement();
        Worker worker = new Worker(id, this, this.keepAliveTime, this.timeUnit, this.queueSize);
        Thread thread = customThreadFactory.newThread(worker);

        worker.setThread(thread);

        thread.start();

        System.out.println("[Pool] Created new worker: MyPool-worker-" + id);
        workers.add(worker);
    }

    public void removeWorker(Worker worker) {
        workers.remove(worker);
    }
}
