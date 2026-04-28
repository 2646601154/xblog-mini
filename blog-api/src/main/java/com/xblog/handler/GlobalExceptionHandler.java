package com.xblog.handler;

import com.xblog.common.ResultCode;
import com.xblog.entity.Result;
import com.xblog.exception.BusinessException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        Result<Void> result = new Result<>();
        result.setCode(e.getResultCode().getCode());
        result.setMessage(e.getMessage());
        result.setErrors(e.getErrors());
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
        Result<Void> result = new Result<>();
        result.setCode(ResultCode.INTERNAL_ERROR.getCode());
        result.setMessage(ResultCode.INTERNAL_ERROR.getMessage());
        return result;
    }

}
