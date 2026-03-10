package com.aryan.springboot.leavemanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);       // 2 threads always alive
        executor.setMaxPoolSize(5);        // max 5 threads under load
        executor.setQueueCapacity(100);    // queue 100 tasks before rejecting
        executor.setThreadNamePrefix("notification-");
        executor.initialize();
        return executor;
    }
}