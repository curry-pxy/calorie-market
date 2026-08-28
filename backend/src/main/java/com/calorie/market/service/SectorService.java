package com.calorie.market.service;

import com.calorie.market.domain.Sector;
import com.calorie.market.dto.SectorRequest;
import com.calorie.market.mapper.SectorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 板块服务。
 */
@Service
@RequiredArgsConstructor
public class SectorService {

    private final SectorMapper sectorMapper;

    public List<Sector> list(Long userId) {
        return sectorMapper.selectByUser(userId);
    }

    public Sector add(Long userId, SectorRequest request) {
        String name = request.getName() == null ? "" : request.getName().trim();
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("板块名不能为空");
        }
        if (name.length() > 10) {
            throw new IllegalArgumentException("板块名不能超过 10 个字");
        }
        String type = request.getType();
        if (!"in".equals(type) && !"out".equals(type)) {
            throw new IllegalArgumentException("板块类型只能是 in 或 out");
        }
        List<Sector> sectors = sectorMapper.selectByUser(userId);
        int maxOrder = 0;
        for (Sector sector : sectors) {
            if (sector.getSortOrder() != null && sector.getSortOrder() > maxOrder) {
                maxOrder = sector.getSortOrder();
            }
        }
        Sector sector = new Sector();
        sector.setUserId(userId);
        sector.setName(name);
        sector.setType(type);
        sector.setCustom(Boolean.TRUE);
        sector.setSortOrder(maxOrder + 1);
        sectorMapper.insert(sector);
        return sector;
    }

    public void delete(Long userId, Long sectorId) {
        Sector sector = sectorMapper.selectByIdAndUser(sectorId, userId);
        if (sector == null) {
            throw new IllegalArgumentException("板块不存在");
        }
        if ("base".equals(sector.getType())) {
            throw new IllegalArgumentException("基础代谢板块不可删除");
        }
        sectorMapper.softDelete(sectorId, userId);
    }
}
