package com.calorie.market.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理，统一转成 ResultModel 返回。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResultModel<Void> handleIllegalArgument(IllegalArgumentException e) {
        return ResultUtil.fail(400, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResultModel<Void> handleException(Exception e) {
        log.error("接口异常", e);
        return ResultUtil.fail(500, "服务器开小差了：" + e.getMessage());
    }
}
