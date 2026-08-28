package com.calorie.market.dto;

import lombok.Data;

/**
 * 登录请求。
 */
@Data
public class LoginRequest {

    /** wx.login 拿到的 code */
    private String code;
}
