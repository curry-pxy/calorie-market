package com.calorie.market.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * K线数据响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KlineResponse {

    /** cal=热量，weight=体重 */
    private String type;

    /** day/week/month/year */
    private String period;

    private String unit;

    private List<Candle> candles;
}
