package com.xblog.exception;

import com.xblog.common.ResultCode;
import lombok.Getter;

import java.util.Map;

@Getter
public class BusinessException extends RuntimeException {

    private final ResultCode resultCode;
    private final Map<String, String> errors;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
        this.errors = null;
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
        this.errors = null;
    }

    public BusinessException(ResultCode resultCode, Map<String, String> errors) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
        this.errors = errors;
    }

    public BusinessException(ResultCode resultCode, String message, Map<String, String> errors) {
        super(message);
        this.resultCode = resultCode;
        this.errors = errors;
    }

}
