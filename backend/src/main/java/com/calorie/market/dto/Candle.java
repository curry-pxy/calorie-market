package com.calorie.market.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * K线单根蜡烛。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Candle {

    private String date;

    private double open;

    private double high;

    private double low;

    private double close;
}
