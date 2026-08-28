package com.calorie.market.dto;

import lombok.Data;

/**
 * 手机号登录/注册请求。
 */
@Data
public class PhoneAuthRequest {

    private String phone;

    private String password;
}
