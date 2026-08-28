package com.calorie.market.dto;

import lombok.Data;

/**
 * 新增记录请求。
 */
@Data
public class RecordRequest {

    private Long sectorId;

    /** 记录日期，不传默认当天 */
    private String date;

    /** 记录时间，不传默认当前时间 */
    private String time;

    private String name;

    private Integer kcal;

    private String qty;
}
