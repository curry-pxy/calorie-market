package com.calorie.market.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 板块（早餐/午餐/运动/自定义板块等）。
 */
@Data
public class Sector {

    private Long id;

    private Long userId;

    private String name;

    /** 类型：in=增加能量，out=减少能量，base=基础代谢 */
    private String type;

    /** 是否用户自定义 */
    private Boolean custom;

    /** 是否已删除（软删除） */
    private Boolean deleted;

    private Integer sortOrder;

    private LocalDateTime createdAt;
}
