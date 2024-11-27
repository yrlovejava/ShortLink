package com.squirrel.shortLink.admin.common.biz.user;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * 用户信息传输过滤器
 */
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    /**
     * 过滤器做登录校验和在上下文中封装用户信息
     * @param servletRequest 这里肯定是http请求
     * @param servletResponse http响应
     * @param filterChain 过滤器链
     */
    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        // 获取用户名
        String username = httpServletRequest.getHeader("username");
        if (StrUtil.isNotBlank(username)) {
            // 获取用户id
            String userId = httpServletRequest.getHeader("userId");
            // 获取用户真实名字
            String realName = httpServletRequest.getHeader("realName");
            // 保存在ThreadLocal中
            UserInfoDTO userInfoDTO = new UserInfoDTO(userId, username, realName);
            UserContext.setUser(userInfoDTO);
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        }finally {
            UserContext.removeUser();
        }
    }
}
