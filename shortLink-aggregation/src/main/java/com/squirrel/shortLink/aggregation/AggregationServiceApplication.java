package com.squirrel.shortLink.aggregation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 短链接聚合应用
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {
        "com.squirrel.shortLink.admin",
        "com.squirrel.shortLink.project",
        "com.squirrel.shortLink.aggregation"
})
@MapperScan(value = {
        "com.squirrel.shortLink.project.dao.mapper",
        "com.squirrel.shortLink.admin.dao.mapper"
})
public class AggregationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AggregationServiceApplication.class, args);
    }
}
