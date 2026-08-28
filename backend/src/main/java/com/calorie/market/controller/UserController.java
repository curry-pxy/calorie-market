package com.calorie.market.controller;

import com.calorie.market.common.ResultModel;
import com.calorie.market.common.ResultUtil;
import com.calorie.market.domain.User;
import com.calorie.market.dto.ProfileRequest;
import com.calorie.market.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户资料接口。
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResultModel<User> profile(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        requireUser(userId);
        return ResultUtil.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public ResultModel<User> updateProfile(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                           @RequestBody ProfileRequest request) {
        requireUser(userId);
        return ResultUtil.success(userService.updateProfile(userId, request));
    }

    private void requireUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("缺少用户标识，请先登录");
        }
    }
}
