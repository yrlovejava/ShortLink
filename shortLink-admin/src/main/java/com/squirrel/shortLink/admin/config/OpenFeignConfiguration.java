package com.squirrel.shortLink.admin.config;

import com.squirrel.shortLink.admin.common.biz.user.UserContext;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenFeign 微服务调用传递用户信息配置
 */
@Configuration
public class OpenFeignConfiguration {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("username", UserContext.getUsername());
            requestTemplate.header("userId", UserContext.getUserId());
            requestTemplate.header("realName", UserContext.getRealName());
        };
    }
}
