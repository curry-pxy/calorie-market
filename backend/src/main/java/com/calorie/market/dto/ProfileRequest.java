package com.calorie.market.dto;

import lombok.Data;

/**
 * 个人资料更新请求。
 */
@Data
public class ProfileRequest {

    private String gender;

    private Integer age;

    private Integer height;

    private Double weight;

    private Integer target;
}
