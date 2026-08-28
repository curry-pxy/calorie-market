package com.calorie.market.controller;

import com.calorie.market.common.ResultModel;
import com.calorie.market.common.ResultUtil;
import com.calorie.market.dto.TodaySummary;
import com.calorie.market.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 今日大盘汇总接口。
 */
@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    @GetMapping("/today")
    public ResultModel<TodaySummary> today(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("缺少用户标识，请先登录");
        }
        return ResultUtil.success(summaryService.today(userId));
    }
}
