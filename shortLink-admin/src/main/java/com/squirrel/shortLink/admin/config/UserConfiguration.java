package com.squirrel.shortLink.admin.config;

import com.squirrel.shortLink.admin.common.biz.user.UserTransmitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 用户配置自动装配
 */
@Configuration
public class UserConfiguration {

    /**
     * 用户信息传递过滤器
     * @param stringRedisTemplate redisTemplate
     * @return FilterRegistrationBean<UserTransmitFilter>
     */
    @Bean
    public FilterRegistrationBean<UserTransmitFilter> filterRegistrationBean(StringRedisTemplate stringRedisTemplate) {
        FilterRegistrationBean<UserTransmitFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(0);
        return registrationBean;
    }
}
