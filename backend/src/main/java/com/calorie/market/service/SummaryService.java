package com.calorie.market.service;

import com.calorie.market.domain.Record;
import com.calorie.market.domain.Sector;
import com.calorie.market.domain.User;
import com.calorie.market.dto.SectorStat;
import com.calorie.market.dto.TodaySummary;
import com.calorie.market.mapper.RecordMapper;
import com.calorie.market.mapper.SectorMapper;
import com.calorie.market.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 今日大盘汇总服务。
 */
@Service
@RequiredArgsConstructor
public class SummaryService {

    private final UserMapper userMapper;
    private final SectorMapper sectorMapper;
    private final RecordMapper recordMapper;

    public TodaySummary today(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        LocalDate today = LocalDate.now();
        List<Record> records = recordMapper.selectByUserAndDate(userId, today);
        List<Sector> sectors = sectorMapper.selectByUser(userId);

        int intake = 0;
        int exercise = 0;
        Map<Long, Integer> valueMap = new LinkedHashMap<Long, Integer>();
        Map<Long, Integer> countMap = new HashMap<Long, Integer>();
        for (Record record : records) {
            Integer kcal = record.getKcal() == null ? 0 : record.getKcal();
            if ("in".equals(record.getSectorType())) {
                intake += kcal;
            } else if ("out".equals(record.getSectorType())) {
                exercise += kcal;
            }
            Long sectorId = record.getSectorId();
            valueMap.put(sectorId, valueMap.getOrDefault(sectorId, 0) + signed(record));
            countMap.put(sectorId, countMap.getOrDefault(sectorId, 0) + 1);
        }

        int bmr = user.getBmr() == null ? 0 : user.getBmr();
        int target = user.getTarget() == null ? 400 : user.getTarget();
        int net = intake - exercise - bmr;

        TodaySummary summary = new TodaySummary();
        summary.setDate(today.toString());
        summary.setIntake(intake);
        summary.setExercise(exercise);
        summary.setBmr(bmr);
        summary.setNet(net);
        summary.setTarget(target);
        summary.setRemain(-target - net);
        if (net <= -target) {
            summary.setStatus("OK");
        } else if (net <= 0) {
            summary.setStatus("WIP");
        } else {
            summary.setStatus("OVER");
        }
        summary.setStreak(computeStreak(userId, today, target, bmr));

        List<SectorStat> sectorStats = new ArrayList<SectorStat>();
        for (Sector sector : sectors) {
            Integer value;
            Integer count;
            if ("base".equals(sector.getType())) {
                value = -bmr;
                count = 1;
            } else {
                value = valueMap.getOrDefault(sector.getId(), 0);
                count = countMap.getOrDefault(sector.getId(), 0);
            }
            sectorStats.add(new SectorStat(sector.getId(), sector.getName(), sector.getType(), value, count));
        }
        summary.setSectors(sectorStats);
        return summary;
    }

    private int signed(Record record) {
        Integer kcal = record.getKcal() == null ? 0 : record.getKcal();
        return "in".equals(record.getSectorType()) ? kcal : -kcal;
    }

    /**
     * 从今天往前数连续达标（净热量小于等于目标赤字）的天数；没有记录的天不算达标。
     */
    private int computeStreak(Long userId, LocalDate today, int target, int bmr) {
        LocalDate start = today.minusDays(59);
        List<Record> range = recordMapper.selectByUserRange(userId, start, today);
        Map<LocalDate, Integer> dayNet = new HashMap<LocalDate, Integer>();
        for (Record record : range) {
            LocalDate date = record.getRecordDate();
            dayNet.put(date, dayNet.getOrDefault(date, 0) + signed(record));
        }
        int streak = 0;
        for (int i = 0; i < 60; i++) {
            LocalDate date = today.minusDays(i);
            Integer net = dayNet.get(date);
            if (net == null) {
                break;
            }
            if (net - bmr <= -target) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }
}
