package com.squirrel.common.convention.exception;

import com.squirrel.common.convention.errorcode.IErrorCode;
import lombok.Getter;

/**
 * 抽象项目中三类异常体系：
 * 1.客户端异常
 * 2.服务端异常
 * 3.远程服务调用异常
 */
@Getter
public abstract class AbstractException extends RuntimeException {

    public final String errorCode;

    public final String errorMessage;

    public AbstractException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable);
        this.errorCode = errorCode.code();
        this.errorMessage = errorCode.message();
    }
}
