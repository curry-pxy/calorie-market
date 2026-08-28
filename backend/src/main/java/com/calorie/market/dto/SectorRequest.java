package com.calorie.market.dto;

import lombok.Data;

/**
 * 新增板块请求。
 */
@Data
public class SectorRequest {

    private String name;

    /** in=增加能量，out=减少能量 */
    private String type;
}
