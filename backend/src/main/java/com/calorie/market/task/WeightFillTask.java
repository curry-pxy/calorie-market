package com.calorie.market.task;

import com.calorie.market.domain.WeightRecord;
import com.calorie.market.mapper.UserMapper;
import com.calorie.market.mapper.WeightMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 体重自动补录任务。
 * 每天 23:59 检查每个用户当天是否称重，若没有则按最近一次体重原样补一条记录，
 * 保证体重 K 线每天都有数据、不留空。任务幂等：已有记录的日子不会重复补。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeightFillTask {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    /** 最多往前补录的天数，防止长期未使用的用户一次性生成大量记录。 */
    private static final int MAX_BACKFILL_DAYS = 30;

    private final UserMapper userMapper;
    private final WeightMapper weightMapper;

    @Scheduled(cron = "0 59 23 * * ?", zone = "Asia/Shanghai")
    public void fillMissingWeight() {
        List<Long> userIds = userMapper.selectAllIds();
        int filled = 0;
        for (Long userId : userIds) {
            filled += fillUser(userId);
        }
        log.info("体重自动补录完成：共 {} 个用户，补录 {} 条记录", userIds.size(), filled);
    }

    private int fillUser(Long userId) {
        LocalDate today = LocalDate.now(ZONE);
        WeightRecord last = weightMapper.selectLatestBefore(userId, today.plusDays(1));
        if (last == null || last.getWeightDate() == null || last.getKg() == null) {
            return 0;
        }
        List<WeightRecord> records = weightMapper.selectByUserRange(userId, last.getWeightDate(), today);
        Map<LocalDate, Double> closeByDate = new HashMap<LocalDate, Double>();
        for (WeightRecord weight : records) {
            if (weight.getWeightDate() != null && weight.getKg() != null) {
                // 列表按日期、时间升序，后写入的即为当日最后一次称重
                closeByDate.put(weight.getWeightDate(), weight.getKg());
            }
        }
        LocalDate start = last.getWeightDate().plusDays(1);
        LocalDate earliest = today.minusDays(MAX_BACKFILL_DAYS - 1);
        if (start.isBefore(earliest)) {
            start = earliest;
        }
        double prevKg = last.getKg();
        String tag = StringUtils.hasText(last.getTag()) ? last.getTag() : "晨重";
        int filled = 0;
        for (LocalDate date = start; !date.isAfter(today); date = date.plusDays(1)) {
            Double real = closeByDate.get(date);
            if (real != null) {
                prevKg = real;
                continue;
            }
            WeightRecord auto = new WeightRecord();
            auto.setUserId(userId);
            auto.setWeightDate(date);
            auto.setWeightTime(LocalTime.of(23, 59));
            auto.setKg(prevKg);
            auto.setTag(tag);
            weightMapper.insert(auto);
            filled++;
        }
        return filled;
    }
}
