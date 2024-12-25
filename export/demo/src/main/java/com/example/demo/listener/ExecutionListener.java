package com.example.demo.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class ExecutionListener implements JobExecutionListener {

  private final ThreadPoolTaskExecutor executor;

  public ExecutionListener(ThreadPoolTaskExecutor executor) {
    this.executor = executor;
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    executor.shutdown();
  }
}
