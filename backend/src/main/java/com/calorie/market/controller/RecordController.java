package com.calorie.market.controller;

import com.calorie.market.common.ResultModel;
import com.calorie.market.common.ResultUtil;
import com.calorie.market.domain.Record;
import com.calorie.market.dto.RecordRequest;
import com.calorie.market.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 每日记录接口。
 */
@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @GetMapping
    public ResultModel<List<Record>> list(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                          @RequestParam(value = "date", required = false) String date) {
        requireUser(userId);
        return ResultUtil.success(recordService.listByDate(userId, date));
    }

    @PostMapping
    public ResultModel<Record> add(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                   @RequestBody RecordRequest request) {
        requireUser(userId);
        return ResultUtil.success(recordService.add(userId, request));
    }

    @DeleteMapping("/{id}")
    public ResultModel<Void> delete(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                    @PathVariable("id") Long recordId) {
        requireUser(userId);
        recordService.delete(userId, recordId);
        return ResultUtil.success();
    }

    private void requireUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("缺少用户标识，请先登录");
        }
    }
}
