package com.example.asyncrestapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Service
public class TaskService {

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private Map<String, Task> taskMap = new HashMap();

    public Task executeLongRunningAsyncTask() {

        Task task = new Task();
        task.setQueueId(UUID.randomUUID().toString());
        taskMap.put(task.getQueueId(), task);

        Callable<TaskResult> taskResultCallable = () -> {

            long threadId = Thread.currentThread().getId();
            log.info("Task queue {} created.", threadId);

            for (int i = 1; i <= 60; i++) {
                try {
                    task.setDescription(String.format("Elapsed time %s sec(s)", i));
                    log.info("Sleeping for 1 second. Elapsed time {} sec(s)", i);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }

            return new TaskResult(0);
        };

        task.setTaskResultFuture(scheduledExecutorService.submit(taskResultCallable));
        return task;
    }

    public Task queryLongRunningAsyncTask(final String queueId) {
        return taskMap.containsKey(queueId) ? taskMap.get(queueId) : new Task();
    }
}
