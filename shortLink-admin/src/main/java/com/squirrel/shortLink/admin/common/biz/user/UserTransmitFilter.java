package com.squirrel.shortLink.admin.common.biz.user;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.squirrel.shortLink.admin.common.convention.exception.ClientException;
import com.squirrel.shortLink.admin.common.convention.result.Results;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

import static com.squirrel.shortLink.admin.common.enums.UserErrorCodeEnum.USER_TOKEN_FAIL;

/**
 * 用户信息传输过滤器
 */
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    private static final List<String> IGNORE_URI = Lists.newArrayList(
            "/api/short-link/admin/v1/user/login",
            "/api/short-link/admin/admin/v1/actual/user/has-username"
    );

    /**
     * 过滤器做登录校验和在上下文中封装用户信息
     * @param servletRequest 这里肯定是http请求
     * @param servletResponse http响应
     * @param filterChain 过滤器链
     * @throws IOException 异常
     * @throws ServletException 异常
     */
    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        // 1.获取请求路劲
        String requestURI = httpServletRequest.getRequestURI();
        // 2.判断是否是需要忽略的路径
        if (!IGNORE_URI.contains(requestURI)) {
            String method = httpServletRequest.getMethod();
            if(!Objects.equals(requestURI, "/api/short-link/v1/user") && Objects.equals(method,"POST")){
                String username = httpServletRequest.getHeader("username");
                String token = httpServletRequest.getHeader("token");
                if (!StrUtil.isAllNotBlank(username, token)) {
                    returnJson((HttpServletResponse) servletResponse, JSON.toJSONString(
                            Results.failure(new ClientException(USER_TOKEN_FAIL))
                    ));
                    return;
                }
                Object userInfoJsonStr;
                try {
                    userInfoJsonStr = stringRedisTemplate.opsForHash().get("login_" + username, token);
                    if (userInfoJsonStr == null) {
                        throw new ClientException(USER_TOKEN_FAIL);
                    }
                }catch (Exception ex){
                    returnJson((HttpServletResponse) servletResponse,  JSON.toJSONString(
                            Results.failure(new ClientException(USER_TOKEN_FAIL))
                    ));
                    return;
                }
                UserInfoDTO userInfoDTO = JSON.parseObject(userInfoJsonStr.toString(), UserInfoDTO.class);
                UserContext.setUser(userInfoDTO);
            }
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        }finally {
            UserContext.removeUser();
        }
    }

    /**
     * 通过浏览器返回 json 字符串
     * @param response http响应
     * @param json json字符串
     */
    private void returnJson(HttpServletResponse response,String json) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.print(json);
        } catch (IOException ignored) {
        }
    }
}
