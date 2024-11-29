package com.squirrel.shortLink.project.config;

import com.squirrel.shortLink.project.mq.consumer.ShortLinkStatsSaveConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.squirrel.shortLink.project.common.constant.RedisKeyConstant.SHORT_LINK_STATS_STREAM_GROUP_KEY;
import static com.squirrel.shortLink.project.common.constant.RedisKeyConstant.SHORT_LINK_STATS_STREAM_TOPIC_KEY;

/**
 * Redis Stream 消息队列配置
 */
@Configuration
@RequiredArgsConstructor
public class RedisStreamConfiguration {

    private final RedisConnectionFactory redisConnectionFactory;
    private final ShortLinkStatsSaveConsumer shortLinkStatsSaveConsumer;

    /**
     * 异步消费redis消息队列的线程池
     * @return 自定义线程池
     */
    @Bean
    public ExecutorService asyncStreamConsumer() {
        AtomicInteger index = new AtomicInteger();
        return new ThreadPoolExecutor(
                1,
                1,
                60,// 空闲线程最大存活时间
                TimeUnit.SECONDS,// 时间单位
                new SynchronousQueue<>(),// 阻塞队列
                // 自定义线程工厂
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("stream-consumer_short-link-stats_" + index.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                },
                // 拒绝策略，抛弃最以前的任务
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
    }

    /**
     * 创建监听容器
     * @param asyncStreamConsumer 自定义线程池
     * @return 监听容器
     */
    @Bean
    public Subscription shortLinkStatsSaveConsumerSubscription(ExecutorService asyncStreamConsumer) {
        // 1.配置监听容器的行为
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

        // 2.创建监听容器配置
        StreamMessageListenerContainer.StreamReadRequest<String> streamReadRequest =
                StreamMessageListenerContainer.StreamReadRequest.builder(
                                // topic就是stream的名称 ReadOffset.lastConsumed() 从上次消费的位置开始读取
                        StreamOffset.create(SHORT_LINK_STATS_STREAM_TOPIC_KEY, ReadOffset.lastConsumed())
                        )
                        .cancelOnError(throwable -> false) //如果在处理过程中出现错误时，不取消消费
                        .consumer(Consumer.from(SHORT_LINK_STATS_STREAM_GROUP_KEY, "stats-consumer"))// 创建一个消费者，设置所属消费组，定义消费者名称
                        .autoAcknowledge(true)// 自动确认消费成功
                        .build();
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer = StreamMessageListenerContainer.create(redisConnectionFactory, options);

        // 3.注册一个消息监听器
        Subscription subscription = listenerContainer.register(streamReadRequest,
                shortLinkStatsSaveConsumer // 消费逻辑的实现
        );

        // 4.开启监听器
        listenerContainer.start();

        // 5.返回监听器的订阅对象
        return subscription;
    }
}
