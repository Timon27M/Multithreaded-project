package org.example.threadpool;

import java.util.concurrent.RejectedExecutionException;

public class DefaultRejectedHandler implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable task, CustomThreadPool pool) {
        System.out.println("[Rejected] Task " + task.toString() + " was rejected due to overload!");
        throw new RuntimeException("Task rejected: " + task.toString());
    }
}
