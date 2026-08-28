package com.calorie.market.controller;

import com.calorie.market.common.ResultModel;
import com.calorie.market.common.ResultUtil;
import com.calorie.market.dto.LoginRequest;
import com.calorie.market.dto.LoginResponse;
import com.calorie.market.dto.PhoneAuthRequest;
import com.calorie.market.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录接口。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResultModel<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResultUtil.success(authService.login(request.getCode()));
    }

    @PostMapping("/wx-login")
    public ResultModel<LoginResponse> wxLogin(@RequestBody LoginRequest request) {
        return ResultUtil.success(authService.login(request.getCode()));
    }

    @PostMapping("/phone/register")
    public ResultModel<LoginResponse> phoneRegister(@RequestBody PhoneAuthRequest request) {
        return ResultUtil.success(authService.registerByPhone(request.getPhone(), request.getPassword()));
    }

    @PostMapping("/phone/login")
    public ResultModel<LoginResponse> phoneLogin(@RequestBody PhoneAuthRequest request) {
        return ResultUtil.success(authService.loginByPhone(request.getPhone(), request.getPassword()));
    }
}
