package com.squirrel.shortLink.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 短链接后管应用
 */
@MapperScan("com.squirrel.shortLink.admin.dao.mapper")
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients("com.squirrel.shortLink.admin.remote")
public class ShortLinkAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortLinkAdminApplication.class, args);
    }
}
