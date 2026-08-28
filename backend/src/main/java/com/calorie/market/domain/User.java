package com.calorie.market.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户。
 */
@Data
public class User {

    private Long id;

    private String openid;

    /** 手机号（手机号注册用户使用） */
    private String phone;

    /** 密码哈希（salt:hash），仅登录校验使用，不对外返回 */
    private String password;

    private String nickname;

    /** 性别：男 / 女 */
    private String gender;

    private Integer age;

    /** 身高，单位 cm */
    private Integer height;

    /** 体重，单位 kg */
    private Double weight;

    /** 每日目标赤字，单位 kcal */
    private Integer target;

    /** 基础代谢，单位 kcal */
    private Integer bmr;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
