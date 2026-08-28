package com.calorie.market.service;

import com.calorie.market.domain.WeightRecord;
import com.calorie.market.dto.WeightRequest;
import com.calorie.market.mapper.WeightMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 体重服务。
 */
@Service
@RequiredArgsConstructor
public class WeightService {

    private final WeightMapper weightMapper;

    public List<WeightRecord> recent(Long userId, int limit) {
        int safeLimit = limit > 0 && limit <= 200 ? limit : 30;
        return weightMapper.selectRecent(userId, safeLimit);
    }

    public WeightRecord add(Long userId, WeightRequest request) {
        if (request.getKg() == null || request.getKg() < 20 || request.getKg() > 300) {
            throw new IllegalArgumentException("体重需在 20-300 kg 之间");
        }
        String tag = StringUtils.hasText(request.getTag()) ? request.getTag().trim() : "晨重";
        if (!"晨重".equals(tag) && !"晚重".equals(tag)) {
            throw new IllegalArgumentException("时段只能是晨重或晚重");
        }
        LocalDate today = LocalDate.now();
        WeightRecord existing = weightMapper.selectByUserDateTag(userId, today, tag);
        if (existing != null) {
            existing.setKg(request.getKg());
            existing.setWeightTime(LocalTime.now());
            weightMapper.updateByUserDateTag(existing);
            return existing;
        }
        WeightRecord weight = new WeightRecord();
        weight.setUserId(userId);
        weight.setWeightDate(today);
        weight.setWeightTime(LocalTime.now());
        weight.setKg(request.getKg());
        weight.setTag(tag);
        weightMapper.insert(weight);
        return weight;
    }
}
