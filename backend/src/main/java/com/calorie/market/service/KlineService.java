package com.calorie.market.service;

import com.calorie.market.domain.Record;
import com.calorie.market.domain.User;
import com.calorie.market.domain.WeightRecord;
import com.calorie.market.dto.Candle;
import com.calorie.market.dto.KlineResponse;
import com.calorie.market.mapper.RecordMapper;
import com.calorie.market.mapper.UserMapper;
import com.calorie.market.mapper.WeightMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * K线数据服务。
 */
@Service
@RequiredArgsConstructor
public class KlineService {

    private static final int HISTORY_DAYS = 1095;

    private final UserMapper userMapper;
    private final RecordMapper recordMapper;
    private final WeightMapper weightMapper;

    public KlineResponse kline(Long userId, String type, String period) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        String safeType = "weight".equals(type) ? "weight" : "cal";
        String safePeriod = normalizePeriod(period);
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(HISTORY_DAYS);

        List<Candle> daily;
        String unit;
        if ("weight".equals(safeType)) {
            daily = buildWeightDaily(userId, start, end);
            unit = "kg";
        } else {
            int bmr = user.getBmr() == null ? 0 : user.getBmr();
            daily = buildCalDaily(userId, start, end, bmr);
            unit = "kcal";
        }
        List<Candle> candles = aggregate(daily, safePeriod);
        return new KlineResponse(safeType, safePeriod, unit, candles);
    }

    private String normalizePeriod(String period) {
        if ("week".equals(period)) {
            return "week";
        }
        if ("month".equals(period)) {
            return "month";
        }
        if ("year".equals(period)) {
            return "year";
        }
        return "day";
    }

    /**
     * 热量日K：开盘=0，收盘=当日净热量（含基础代谢分摊），最高/最低=日内累计极值。
     */
    private List<Candle> buildCalDaily(Long userId, LocalDate start, LocalDate end, int bmr) {
        List<Record> records = recordMapper.selectByUserRange(userId, start, end);
        Map<LocalDate, List<Record>> byDate = new TreeMap<LocalDate, List<Record>>();
        for (Record record : records) {
            if (record.getRecordDate() == null) {
                continue;
            }
            List<Record> list = byDate.get(record.getRecordDate());
            if (list == null) {
                list = new ArrayList<Record>();
                byDate.put(record.getRecordDate(), list);
            }
            list.add(record);
        }
        double perHour = bmr / 24.0;
        List<Candle> candles = new ArrayList<Candle>();
        for (Map.Entry<LocalDate, List<Record>> entry : byDate.entrySet()) {
            List<Record> list = entry.getValue();
            list.sort((a, b) -> {
                if (a.getRecordTime() == null || b.getRecordTime() == null) {
                    return 0;
                }
                return a.getRecordTime().compareTo(b.getRecordTime());
            });
            double cum = 0;
            double high = 0;
            double low = 0;
            double prevHour = 0;
            for (Record record : list) {
                double hour = toHour(record);
                cum -= perHour * (hour - prevHour);
                prevHour = hour;
                cum += signed(record);
                high = Math.max(high, cum);
                low = Math.min(low, cum);
            }
            cum -= perHour * (24 - prevHour);
            low = Math.min(low, cum);
            candles.add(new Candle(entry.getKey().toString(), 0, Math.round(high), Math.round(low), Math.round(cum)));
        }
        return candles;
    }

    private double toHour(Record record) {
        if (record.getRecordTime() == null) {
            return 0;
        }
        return record.getRecordTime().getHour() + record.getRecordTime().getMinute() / 60.0;
    }

    private int signed(Record record) {
        Integer kcal = record.getKcal() == null ? 0 : record.getKcal();
        return "in".equals(record.getSectorType()) ? kcal : -kcal;
    }

    /**
     * 体重日K：开盘=当日第一次称重，收盘=最后一次，最高/最低=当日极值。
     */
    private List<Candle> buildWeightDaily(Long userId, LocalDate start, LocalDate end) {
        List<WeightRecord> weights = weightMapper.selectByUserRange(userId, start, end);
        Map<LocalDate, List<WeightRecord>> byDate = new TreeMap<LocalDate, List<WeightRecord>>();
        for (WeightRecord weight : weights) {
            if (weight.getWeightDate() == null || weight.getKg() == null) {
                continue;
            }
            List<WeightRecord> list = byDate.get(weight.getWeightDate());
            if (list == null) {
                list = new ArrayList<WeightRecord>();
                byDate.put(weight.getWeightDate(), list);
            }
            list.add(weight);
        }
        List<Candle> candles = new ArrayList<Candle>();
        for (Map.Entry<LocalDate, List<WeightRecord>> entry : byDate.entrySet()) {
            List<WeightRecord> list = entry.getValue();
            double open = list.get(0).getKg();
            double close = list.get(list.size() - 1).getKg();
            double high = open;
            double low = open;
            for (WeightRecord weight : list) {
                high = Math.max(high, weight.getKg());
                low = Math.min(low, weight.getKg());
            }
            candles.add(new Candle(entry.getKey().toString(), open, high, low, close));
        }
        return candles;
    }

    /**
     * 按周期聚合：day 取最近 60 根，week/month/year 取最近 78/36/6 根。
     */
    private List<Candle> aggregate(List<Candle> daily, String period) {
        if ("day".equals(period)) {
            return last(daily, 60);
        }
        if ("week".equals(period)) {
            return last(aggregateByKey(daily, "week"), 78);
        }
        if ("month".equals(period)) {
            return last(aggregateByKey(daily, "month"), 36);
        }
        return last(aggregateByKey(daily, "year"), 6);
    }

    private List<Candle> aggregateByKey(List<Candle> daily, String keyType) {
        Map<String, Candle> map = new LinkedHashMap<String, Candle>();
        for (Candle candle : daily) {
            String key = keyOf(candle.getDate(), keyType);
            Candle merged = map.get(key);
            if (merged == null) {
                map.put(key, new Candle(key, candle.getOpen(), candle.getHigh(), candle.getLow(), candle.getClose()));
            } else {
                merged.setHigh(Math.max(merged.getHigh(), candle.getHigh()));
                merged.setLow(Math.min(merged.getLow(), candle.getLow()));
                merged.setClose(candle.getClose());
            }
        }
        return new ArrayList<Candle>(map.values());
    }

    private String keyOf(String dateStr, String keyType) {
        if ("month".equals(keyType)) {
            return dateStr.length() >= 7 ? dateStr.substring(0, 7) : dateStr;
        }
        if ("year".equals(keyType)) {
            return dateStr.length() >= 4 ? dateStr.substring(0, 4) : dateStr;
        }
        LocalDate date = LocalDate.parse(dateStr);
        int dow = date.getDayOfWeek().getValue();
        return date.minusDays(dow - 1).toString();
    }

    private List<Candle> last(List<Candle> candles, int size) {
        if (candles.size() <= size) {
            return candles;
        }
        return new ArrayList<Candle>(candles.subList(candles.size() - size, candles.size()));
    }
}
