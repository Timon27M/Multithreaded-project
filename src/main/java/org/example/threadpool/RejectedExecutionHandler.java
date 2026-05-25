package org.example.threadpool;

public interface RejectedExecutionHandler {
    void rejectedExecution(Runnable task, CustomThreadPool pool);
}
