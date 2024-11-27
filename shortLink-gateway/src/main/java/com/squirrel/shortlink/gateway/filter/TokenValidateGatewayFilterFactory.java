package com.squirrel.shortlink.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.squirrel.shortlink.gateway.config.Config;
import com.squirrel.shortlink.gateway.dto.GatewayErrorResult;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

import static com.squirrel.shortlink.gateway.constant.RedisCacheConstant.USER_LOGIN_KEY;

/**
 * SpringCloud Gateway Token 拦截器
 * WebFlux网关
 */
@Component
public class TokenValidateGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    private final StringRedisTemplate stringRedisTemplate;

    public TokenValidateGatewayFilterFactory(StringRedisTemplate stringRedisTemplate) {
        super(Config.class);
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * token过滤
     * @param config 过滤器配置
     * @return GatewayFilter
     */
    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            // 获取 request 对象
            ServerHttpRequest request = exchange.getRequest();
            // 获取请求路径
            String requestPath = request.getPath().toString();
            // 获取请求方式
            String requestMethod = request.getMethod().name();
            if (!isPathInWhiteList(requestPath,requestMethod,config.getWhitePathList())) {
                // 不在白名单中，需要拦截处理
                String username = request.getHeaders().getFirst("username");
                String token = request.getHeaders().getFirst("token");
                Object userInfo;
                // 是否登录判断
                if (StringUtils.hasText(username) // 用户名不为空
                        && StringUtils.hasText(token) // token不为空
                        && (userInfo = stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username,token)) != null // redis中有用户登录信息
                ) {
                    // 将 userId realName 等信息封装在 request 中
                    JSONObject userInfoJsonObject = JSON.parseObject(userInfo.toString());
                    ServerHttpRequest.Builder builder = exchange.getRequest().mutate().headers(httpHeaders -> {
                        httpHeaders.set("userId", userInfoJsonObject.getString("userId"));
                        httpHeaders.set("realName", userInfoJsonObject.getString("realName"));
                    });
                    // 放行
                    return chain.filter(exchange.mutate().request(builder.build()).build());
                }
                // 获取 response
                ServerHttpResponse response = exchange.getResponse();
                // 设置响应码
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                // 异步写入响应体
                return response.writeWith(
                        // 懒加载的 Mono,只有在订阅时才会执行提供的数据生成逻辑
                        Mono.fromSupplier(() -> {
                    // DataBuffer 是 SpringWebFlux 中的数据缓冲区
                    DataBufferFactory bufferFactory = response.bufferFactory();
                    // 构建错误信息
                    GatewayErrorResult resultMessage = GatewayErrorResult.builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .message("Token validation error")
                            .build();
                    // json序列化然后封装为DataBuffer，供 response.writeWith方法写入响应体
                    return bufferFactory.wrap(JSON.toJSONString(resultMessage).getBytes());
                }));
            }
            return chain.filter(exchange);
        });
    }

    /**
     * 查询是否在白名单中
     * @param requestPath 请求路径
     * @param requestMethod 请求方式
     * @param whiteList 白名单
     * @return 是否在白名单中
     */
    private boolean isPathInWhiteList(String requestPath, String requestMethod, List<String> whiteList) {
        return (!CollectionUtils.isEmpty(whiteList) && whiteList.stream().anyMatch(requestPath::startsWith) // 白名单不为空并且路径以白名单的某个前缀开头
                || (Objects.equals(requestPath,"/api/short-link/admin/v1/user") && Objects.equals(requestMethod, "POST")) // 或者是注册请求
        );
    }
}
