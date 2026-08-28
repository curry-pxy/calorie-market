package com.calorie.market.common;

/**
 * 统一返回工具类。
 */
public final class ResultUtil {

    private ResultUtil() {
    }

    public static <T> ResultModel<T> success(T data) {
        return new ResultModel<T>(0, "ok", data);
    }

    public static ResultModel<Void> success() {
        return new ResultModel<Void>(0, "ok", null);
    }

    public static <T> ResultModel<T> fail(int code, String message) {
        return new ResultModel<T>(code, message, null);
    }
}
