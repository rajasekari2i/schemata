package com.opsbeach.connect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.opsbeach.connect.scheduler.SyncScheduler;


@Configuration
@EnableScheduling
public class SchedulerConfig {

  // Number of threads allocated for OS related tasks
  public static final int OS_THREAD_RESERVE = 2;
  // Minimal acceptable thread pool size
  public static final int MIN_THREAD_THRESHOLD = 1;

  @Bean
  public SyncScheduler syncScheduler() {
    return new SyncScheduler();
  }

  @Bean
  public TaskScheduler getPoolScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
    scheduler.setPoolSize(getThreadPoolSize());
    scheduler.initialize();
    return scheduler;
  }

  public int getThreadPoolSize() {
    return Math.max((Runtime.getRuntime().availableProcessors() - OS_THREAD_RESERVE), MIN_THREAD_THRESHOLD);
  }
}
