package com.tt.retrieval.common;

import lombok.Data;

/**
 * @author LuHongGang
 * @version 1.0
 */
@Data
public class BaseException extends RuntimeException {
    private Integer code;
    private String message;

    private BaseException() {
    }

    public BaseException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public BaseException(Throwable cause, Integer code, String message) {
        super(cause);
        this.code = code;
        this.message = message;
    }

    public BaseException(ResultCodeEnum serverError) {
        this.code = serverError.getCode();
        this.message = serverError.getMessage();
    }

    @Override
    public String toString() {
        return "BaseException {" + "code=" + code + ", message='" + message + '}';
    }
}
