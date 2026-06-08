package com.xblog.handler;

import com.xblog.common.enums.ResultCode;
import com.xblog.entity.Result;
import com.xblog.common.exception.BusinessException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        Result<Void> result = new Result<>();
        result.setCode(e.getResultCode().getCode());
        // sensitive=true 的错误码强制使用 ResultCode 的 generic message，
        // 忽略 throw 处传入的自定义 message（防止开发者在 throw 时误传具体业务原因）
        String clientMessage = e.getResultCode().isSensitive()
                ? e.getResultCode().getMessage()
                : e.getMessage();
        result.setMessage(clientMessage);
        result.setErrors(e.getErrors());
        return result;
    }
    @ExceptionHandler(JwtException.class)
    public Result<Void> handleJwtException(JwtException e) {
        log.warn("JWT 异常: {}", e.getMessage());
        Result<Void> result = new Result<>();
        result.setCode(ResultCode.AUTH_TOKEN_INVALID.getCode());
        result.setMessage("Token 无效");
        return result;
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        Result<Void> result = new Result<>();
        result.setCode(ResultCode.BAD_REQUEST.getCode());
        result.setMessage(ResultCode.BAD_REQUEST.getMessage());
        result.setErrors(errors);
        return result;
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        Result<Void> result = new Result<>();
        result.setCode(ResultCode.INTERNAL_ERROR.getCode());
        result.setMessage(ResultCode.INTERNAL_ERROR.getMessage());
        return result;
    }

}
