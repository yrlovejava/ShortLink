/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squirrel.shortLink.admin.common.biz.user;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.squirrel.shortLink.admin.common.convention.exception.ClientException;
import com.squirrel.shortLink.admin.common.convention.result.Results;
import com.squirrel.shortLink.admin.config.UserFlowRiskControlConfiguration;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import static com.squirrel.shortLink.admin.common.convention.errorcode.BaseErrorCode.FLOW_LIMIT_ERROR;

/**
 * 用户流量风控过滤器
 */
@Slf4j
@RequiredArgsConstructor
public class UserFlowRiskControlFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserFlowRiskControlConfiguration userFlowRiskControlConfiguration;

    private static final DefaultRedisScript<Long> USER_FLOW_RISK_CONTROL_SCRIPT;

    private static final String USER_FLOW_RISK_CONTROL_LUA_SCRIPT_PATH = "lua/user_flow_risk_control.lua";

    /**
     * 初始化lua脚本
     */
   static {
        log.info("init user flow risk control script");
        USER_FLOW_RISK_CONTROL_SCRIPT = new DefaultRedisScript<>();
        USER_FLOW_RISK_CONTROL_SCRIPT.setScriptSource(new ResourceScriptSource(new ClassPathResource(USER_FLOW_RISK_CONTROL_LUA_SCRIPT_PATH)));
        USER_FLOW_RISK_CONTROL_SCRIPT.setResultType(Long.class);
    }

    /**
     * 流量限制
     * @param servletRequest http请求
     * @param servletResponse http响应
     * @param filterChain 过滤器链
     * @throws IOException io异常
     * @throws ServletException servlet异常
     */
    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String uri = ((HttpServletRequest) servletRequest).getRequestURI();
        // 排除指定路径
        if (uri.startsWith("/doc.html")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        // 1.获取用户名
        String username = Optional.ofNullable(UserContext.getUsername()).orElse("other");
        // 2.执行lua脚本，获取滑动窗口中的数量
        Long result;
        try {
            result = stringRedisTemplate.execute(USER_FLOW_RISK_CONTROL_SCRIPT, Lists.newArrayList(username),userFlowRiskControlConfiguration.getTimeWindow());
        }catch (Throwable ex) {
            log.error("执行用户请求流量限制lua脚本出错",ex);
            returnJson((HttpServletResponse) servletResponse, JSON.toJSONString(Results.failure(new ClientException(FLOW_LIMIT_ERROR))));
            return;
        }
        // 3.如果数量大于限制值
        if (result == null || result > userFlowRiskControlConfiguration.getMaxAccessCount()) {
            returnJson((HttpServletResponse) servletResponse, JSON.toJSONString(Results.failure(new ClientException(FLOW_LIMIT_ERROR))));
            return;
        }
        // 4.放行
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * 返回json响应
     * @param response http响应
     * @param json json数据
     * @throws Exception 可能抛出的异常
     */
    private void returnJson(HttpServletResponse response,String json) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=utf-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.print(json);
        }
    }
}
