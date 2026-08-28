package com.calorie.market.controller;

import com.calorie.market.common.ResultModel;
import com.calorie.market.common.ResultUtil;
import com.calorie.market.domain.Sector;
import com.calorie.market.dto.SectorRequest;
import com.calorie.market.service.SectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 板块接口。
 */
@RestController
@RequestMapping("/api/sectors")
@RequiredArgsConstructor
public class SectorController {

    private final SectorService sectorService;

    @GetMapping
    public ResultModel<List<Sector>> list(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        requireUser(userId);
        return ResultUtil.success(sectorService.list(userId));
    }

    @PostMapping
    public ResultModel<Sector> add(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                   @RequestBody SectorRequest request) {
        requireUser(userId);
        return ResultUtil.success(sectorService.add(userId, request));
    }

    @DeleteMapping("/{id}")
    public ResultModel<Void> delete(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                    @PathVariable("id") Long sectorId) {
        requireUser(userId);
        sectorService.delete(userId, sectorId);
        return ResultUtil.success();
    }

    private void requireUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("缺少用户标识，请先登录");
        }
    }
}
