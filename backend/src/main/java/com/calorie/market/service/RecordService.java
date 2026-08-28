package com.calorie.market.service;

import com.calorie.market.domain.Record;
import com.calorie.market.domain.Sector;
import com.calorie.market.dto.RecordRequest;
import com.calorie.market.mapper.RecordMapper;
import com.calorie.market.mapper.SectorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 每日记录服务。
 */
@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordMapper recordMapper;
    private final SectorMapper sectorMapper;

    public List<Record> listByDate(Long userId, String dateStr) {
        return recordMapper.selectByUserAndDate(userId, parseDate(dateStr, LocalDate.now()));
    }

    public Record add(Long userId, RecordRequest request) {
        if (request.getSectorId() == null) {
            throw new IllegalArgumentException("板块不能为空");
        }
        Sector sector = sectorMapper.selectByIdAndUser(request.getSectorId(), userId);
        if (sector == null) {
            throw new IllegalArgumentException("板块不存在");
        }
        if ("base".equals(sector.getType())) {
            throw new IllegalArgumentException("基础代谢板块不能手动记录");
        }
        String name = request.getName() == null ? "" : request.getName().trim();
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("名称不能为空");
        }
        if (name.length() > 30) {
            throw new IllegalArgumentException("名称不能超过 30 个字");
        }
        if (request.getKcal() == null || request.getKcal() <= 0 || request.getKcal() > 5000) {
            throw new IllegalArgumentException("热量需在 1-5000 kcal 之间");
        }
        Record record = new Record();
        record.setUserId(userId);
        record.setSectorId(request.getSectorId());
        record.setRecordDate(parseDate(request.getDate(), LocalDate.now()));
        record.setRecordTime(parseTime(request.getTime(), LocalTime.now()));
        record.setName(name);
        record.setKcal(request.getKcal());
        record.setQty(request.getQty());
        recordMapper.insert(record);
        record.setSectorName(sector.getName());
        record.setSectorType(sector.getType());
        return record;
    }

    public void delete(Long userId, Long recordId) {
        int rows = recordMapper.deleteByIdAndUser(recordId, userId);
        if (rows == 0) {
            throw new IllegalArgumentException("记录不存在");
        }
    }

    private LocalDate parseDate(String dateStr, LocalDate defaultDate) {
        if (!StringUtils.hasText(dateStr)) {
            return defaultDate;
        }
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("日期格式不正确：" + dateStr);
        }
    }

    private LocalTime parseTime(String timeStr, LocalTime defaultTime) {
        if (!StringUtils.hasText(timeStr)) {
            return defaultTime;
        }
        try {
            return LocalTime.parse(timeStr.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("时间格式不正确：" + timeStr);
        }
    }
}
