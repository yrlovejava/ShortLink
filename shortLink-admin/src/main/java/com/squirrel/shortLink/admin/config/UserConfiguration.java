package com.squirrel.shortLink.admin.config;

import com.squirrel.shortLink.admin.common.biz.user.UserFlowRiskControlFilter;
import com.squirrel.shortLink.admin.common.biz.user.UserTransmitFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
     * @return FilterRegistrationBean<UserTransmitFilter>
     */
    @Bean
    public FilterRegistrationBean<UserTransmitFilter> filterRegistrationBean() {
        FilterRegistrationBean<UserTransmitFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new UserTransmitFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(0);
        return registrationBean;
    }

    /**
     * 用户操作流量风控过滤器
     * @param stringRedisTemplate redis操作模板
     * @param userFlowRiskControlConfiguration 用户流量风控配置
     * @return FilterRegistrationBean<UserFlowRiskControllerFilter>
     */
    @Bean
    @ConditionalOnProperty(name = "short-link.flow-limit.enable", havingValue = "true")
    public FilterRegistrationBean<UserFlowRiskControlFilter> globalUserFlowRiskControlFilter(
            StringRedisTemplate stringRedisTemplate,
            UserFlowRiskControlConfiguration userFlowRiskControlConfiguration
    ) {
        FilterRegistrationBean<UserFlowRiskControlFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new UserFlowRiskControlFilter(stringRedisTemplate, userFlowRiskControlConfiguration));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(0);
        return registrationBean;
    }
}
