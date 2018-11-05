package com.example.asyncrestapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Controller
public class AsyncTaskController {

    private static final String QUERY_URL = "/resultAsyncTask?queue=%s";
    private static final String RESULT_URL = "/resultAsyncTask?queue=%s";

    @Autowired
    public TaskService taskService;

    @RequestMapping(value = "/executeAsynTask", method = RequestMethod.GET)
    public ResponseEntity<Task> executeAsynTask() {

        Task task = taskService.executeLongRunningAsyncTask();
        task.setHref(String.format("/queryAsyncTask?queue=%s", task.getQueueId()));

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, task.getHref())
                .body(task);
    }

    @RequestMapping(value = "/queryAsyncTask", method = RequestMethod.GET)
    public ResponseEntity<Task> queryAsynTask(@RequestParam("queue") String queueId) {

        Task task = taskService.queryLongRunningAsyncTask(queueId);
        Future<TaskResult> future = task.getTaskResultFuture();

        if (null == future) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(task);
        } else if (future.isDone()) {
            task.setHref(String.format(QUERY_URL, task.getQueueId()));
            return ResponseEntity
                    .status(HttpStatus.SEE_OTHER)
                    .header(HttpHeaders.LOCATION, String.format(QUERY_URL, task.getQueueId()))
                    .body(task);
        }

        task.setHref(String.format(QUERY_URL, task.getQueueId()));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(task);
    }

    @RequestMapping(value = "/resultAsyncTask", method = RequestMethod.GET)
    public ResponseEntity<?> resultAsyncTask(@RequestParam("queue") String queueId) {

        Task task = taskService.queryLongRunningAsyncTask(queueId);
        Future<TaskResult> future = task.getTaskResultFuture();

        if (null == future) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(task);
        } else if (future.isDone()) {
            try {
                task.setHref(String.format(RESULT_URL, task.getQueueId()));
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(future.get());
            } catch (InterruptedException | ExecutionException e) {
                log.warn("Exception thrown!!! While calling future.get(), message", e.getMessage());
            }
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(task);
    }
}
