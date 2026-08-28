package com.calorie.market.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一接口返回结构。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultModel<T> {

    /** 0 表示成功，非 0 表示失败 */
    private int code;

    private String message;

    private T data;
}
