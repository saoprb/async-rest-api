package com.example.asyncrestapi;

import lombok.Data;

import java.util.concurrent.Future;

@Data
public class Task {
    private Future<TaskResult> taskResultFuture;
    private String queueId;
    private String description;
    private String href;
}
