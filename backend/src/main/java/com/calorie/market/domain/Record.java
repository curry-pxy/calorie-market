package com.calorie.market.domain;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 每日记录（吃/喝/运动）。
 */
@Data
public class Record {

    private Long id;

    private Long userId;

    private Long sectorId;

    /** 关联板块名称，查询时带出 */
    private String sectorName;

    /** 板块类型，查询时带出 */
    private String sectorType;

    private LocalDate recordDate;

    private LocalTime recordTime;

    private String name;

    /** 热量绝对值（摄入或消耗），单位 kcal */
    private Integer kcal;

    /** 份量或时长，如 1碗 / 45分钟 */
    private String qty;

    private LocalDateTime createdAt;
}
