package com.calorie.market.controller;

import com.calorie.market.common.ResultModel;
import com.calorie.market.common.ResultUtil;
import com.calorie.market.dto.KlineResponse;
import com.calorie.market.service.KlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * K线数据接口。
 */
@RestController
@RequestMapping("/api/kline")
@RequiredArgsConstructor
public class KlineController {

    private final KlineService klineService;

    @GetMapping
    public ResultModel<KlineResponse> kline(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                            @RequestParam(value = "type", required = false) String type,
                                            @RequestParam(value = "period", required = false) String period) {
        if (userId == null) {
            throw new IllegalArgumentException("缺少用户标识，请先登录");
        }
        return ResultUtil.success(klineService.kline(userId, type, period));
    }
}
