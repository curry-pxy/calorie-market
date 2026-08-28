package com.calorie.market.controller;

import com.calorie.market.common.ResultModel;
import com.calorie.market.common.ResultUtil;
import com.calorie.market.domain.WeightRecord;
import com.calorie.market.dto.WeightRequest;
import com.calorie.market.service.WeightService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 体重接口。
 */
@RestController
@RequestMapping("/api/weights")
@RequiredArgsConstructor
public class WeightController {

    private final WeightService weightService;

    @GetMapping
    public ResultModel<List<WeightRecord>> recent(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                  @RequestParam(value = "limit", required = false) Integer limit) {
        if (userId == null) {
            throw new IllegalArgumentException("缺少用户标识，请先登录");
        }
        return ResultUtil.success(weightService.recent(userId, limit == null ? 30 : limit));
    }

    @PostMapping
    public ResultModel<WeightRecord> add(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                         @RequestBody WeightRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("缺少用户标识，请先登录");
        }
        return ResultUtil.success(weightService.add(userId, request));
    }
}
