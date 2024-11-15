package com.squirrel.shortLink.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.squirrel.shortLink.admin.dao.mapper")
@SpringBootApplication
public class ShortLinkAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortLinkAdminApplication.class, args);
    }
}
