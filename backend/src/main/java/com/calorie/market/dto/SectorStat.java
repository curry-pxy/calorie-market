package com.calorie.market.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 今日板块统计。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SectorStat {

    private Long sectorId;

    private String name;

    private String type;

    private Integer value;

    private Integer count;
}
