package com.calorie.market.dto;

import lombok.Data;

/**
 * 新增体重请求。
 */
@Data
public class WeightRequest {

    private Double kg;

    /** 晨重 / 晚重 */
    private String tag;
}
