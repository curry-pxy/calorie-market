package com.calorie.market.dto;

import lombok.Data;

import java.util.List;

/**
 * 今日大盘汇总。
 */
@Data
public class TodaySummary {

    private String date;

    private Integer intake;

    private Integer exercise;

    private Integer bmr;

    private Integer net;

    private Integer target;

    /** OK=已达标，WIP=进行中，OVER=超标 */
    private String status;

    /** 距离目标的差额（负数为已超额） */
    private Integer remain;

    private Integer streak;

    private List<SectorStat> sectors;
}
