package com.clarkrpc.utils.concurrent.threadpool;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 线程池自定义配置类，可根据需求修改配置参数。
 */
@Setter
@Getter
public class CustomThreadPoolConfig {
    /**
     * 线程池默认参数
     */
    private static final int DEFAULT_CORE_POOL_SIZE = 10; //默认核心线程数
    private static final int DEFAULT_MAXIMUM_POOL_SIZE_SIZE = 100; //默认最大线程数
    private static final int DEFAULT_KEEP_ALIVE_TIME = 1; //默认存活时间 ttl
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;//默认单位1分钟
    private static final int DEFAULT_BLOCKING_QUEUE_CAPACITY = 100;//默认阻塞队列容量
    private static final int BLOCKING_QUEUE_CAPACITY = 100;//阻塞队列容量
    /**
     * 可配置参数
     */
    private int corePoolSize = DEFAULT_CORE_POOL_SIZE;
    private int maximumPoolSize = DEFAULT_MAXIMUM_POOL_SIZE_SIZE;
    private long keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
    private TimeUnit unit = DEFAULT_TIME_UNIT;
    // 使用有界队列
    private BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);//有节阻塞队列
}
