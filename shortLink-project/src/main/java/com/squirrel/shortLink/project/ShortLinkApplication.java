package com.squirrel.shortLink.project;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 短链接应用
 */
@SpringBootApplication
@MapperScan("com.squirrel.shortLink.project.dao.mapper")
@EnableTransactionManagement
@EnableDiscoveryClient
@EnableAspectJAutoProxy(exposeProxy = true)
public class ShortLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortLinkApplication.class, args);
    }
}
