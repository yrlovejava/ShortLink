package com.squirrel.shortLink.admin.common.web;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.squirrel.common.convention.errorcode.BaseErrorCode;
import com.squirrel.common.convention.exception.AbstractException;
import com.squirrel.common.convention.result.Result;
import com.squirrel.common.convention.result.Results;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;
import java.util.Optional;

/**
 * 全局异常处理器
 */
@Component("globalExceptionHandlerByAdmin")
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 拦截参数验证异常
     * @param request http请求
     * @param ex 参数验证异常
     * @return Result
     */
    @SneakyThrows // 用于在方法中抛出受检异常
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result validExceptionHandler(HttpServletRequest request, MethodArgumentNotValidException ex) {
        // 获取包含验证失败的所有错误信息
        BindingResult bindingResult = ex.getBindingResult();
        // 从 bindingResult 中提取出第一个字段错误
        FieldError firstFieldError = CollectionUtil.getFirst(bindingResult.getFieldErrors());
        // 获取第一个字段错误的默认错误信息，如果没有错误信息，则返回空字符串
        String exceptionStr = Optional.ofNullable(firstFieldError)
                .map(FieldError::getDefaultMessage)
                .orElse(StrUtil.EMPTY);
        log.error("[{}] {} [ex] {}",request.getMethod(),getUrl(request),exceptionStr);
        return Results.failure(BaseErrorCode.CLIENT_ERROR.code(), exceptionStr);
    }

    /**
     * 拦截应用内抛出的异常
     * @param request http请求
     * @param ex 应用内抛出的异常
     * @return Result
     */
    public Result abstractExceptionHandler(HttpServletRequest request, AbstractException ex) {
        if (ex.getCause() != null){
            log.error("[{}] {} [ex] {}",request.getMethod(),request.getRequestURL().toString(),ex,ex.getCause());
            return Results.failure(ex);
        }
        log.error("[{}] {} [ex] {}", request.getMethod(), request.getRequestURL().toString(), ex.toString());
        return Results.failure(ex);
    }

    /**
     * 拦截未捕获异常
     * @param request http请求
     * @param throwable 未捕获异常
     * @return Result
     */
    @ExceptionHandler(value = Throwable.class)
    public Result defaultErrorHandler(HttpServletRequest request, Throwable throwable) {
        log.error("[{}] {}",request.getMethod(),getUrl(request),throwable);
        // 注意，此处是为了聚合模式添加的代码，正常不需要该判断
        if (Objects.equals(throwable.getClass().getSuperclass().getSimpleName(),AbstractException.class.getName())){
            String errorCode = ReflectUtil.getFieldValue(throwable, "errorCode").toString();
            String errorMessage = ReflectUtil.getFieldValue(throwable, "errorMessage").toString();
            return Results.failure(errorCode,errorMessage);
        }
        return Results.failure();
    }

    /**
     * 获取url
     * @param request http请求
     * @return url
     */
    private String getUrl(HttpServletRequest request) {
        if (StringUtils.isEmpty(request.getQueryString())) {
            return request.getRequestURL().toString();
        }
        return request.getRequestURL().toString() + "?" + request.getQueryString();
    }
}
