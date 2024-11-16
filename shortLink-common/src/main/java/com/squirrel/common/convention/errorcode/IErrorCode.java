package com.squirrel.common.convention.errorcode;

/**
 * 平台错误码
 */
public interface IErrorCode {

    /**
     * 错误码
     * @return code
     */
    String code();

    /**
     * 错误信息
     * @return message
     */
    String message();
}
