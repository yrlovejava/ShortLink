package com.squirrel.project.config;

import com.squirrel.project.mq.consumer.ShortLinkStatsSaveConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Redis Stream 消息队列配置
 */
@Configuration
@RequiredArgsConstructor
public class RedisStreamConfiguration {

    private final RedisConnectionFactory redisConnectionFactory;
    private final ShortLinkStatsSaveConsumer shortLinkStatsSaveConsumer;

    @Value("${spring.data.redis.channel-topic.short-link-stats}")
    private String topic;

    @Value("${spring.data.redis.channel-topic.short-link-stats-group}")
    private String group;

    @Bean
    public ExecutorService asyncStreamConsumer() {
        AtomicInteger index = new AtomicInteger();
        // 获取处理器的数量
        int processors = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(
                processors,// 核心线程数为处理器数量
                processors + processors >> 1,// 最大空闲线程数为: 3 * processors / 2
                60,// 空闲线程最大存活时间
                TimeUnit.SECONDS,// 时间单位
                new LinkedBlockingDeque<>(),// 阻塞队列
                // 自定义拒绝策略，这里是创建新的线程去执行任务
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("stream-consumer_short-link-stats_" + index.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                }
        );
    }

    @Bean(initMethod = "start",destroyMethod = "stop")
    public StreamMessageListenerContainer<String, MapRecord<String,String,String>> streamMessageListenerContainer(ExecutorService asyncStreamConsumer) {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String,MapRecord<String,String,String>> options =
            StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                    .builder()
                    // 一次性最多获取多少条消息
                    .batchSize(10)
                    // 执行从 Stream 拉取到消息的任务流程
                    .executor(asyncStreamConsumer)
                    // 如果没有拉取到消息，需要阻塞的时间。不能大于 ${spring.data.redis.timeout}，否则会超时
                    .pollTimeout(Duration.ofSeconds(3))
                    .build();
        StreamMessageListenerContainer<String,MapRecord<String,String,String>> streamMessageListenerContainer =
                StreamMessageListenerContainer.create(redisConnectionFactory,options);
        streamMessageListenerContainer.receiveAutoAck(
                Consumer.from(group,"stats-consumer"),
                StreamOffset.create(topic, ReadOffset.lastConsumed()),shortLinkStatsSaveConsumer
        );
        return streamMessageListenerContainer;
    }
}
