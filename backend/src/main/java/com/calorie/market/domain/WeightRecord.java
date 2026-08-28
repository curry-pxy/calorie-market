package com.calorie.market.domain;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 体重记录。
 */
@Data
public class WeightRecord {

    private Long id;

    private Long userId;

    private LocalDate weightDate;

    private LocalTime weightTime;

    /** 体重，单位 kg */
    private Double kg;

    /** 时段：晨重 / 晚重 */
    private String tag;

    private LocalDateTime createdAt;
}
