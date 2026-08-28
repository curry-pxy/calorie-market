package com.calorie.market.mapper;

import com.calorie.market.domain.WeightRecord;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 体重 Mapper。
 */
public interface WeightMapper {

    List<WeightRecord> selectByUserRange(@Param("userId") Long userId,
                                         @Param("start") LocalDate start,
                                         @Param("end") LocalDate end);

    List<WeightRecord> selectRecent(@Param("userId") Long userId, @Param("limit") int limit);

    WeightRecord selectByUserDateTag(@Param("userId") Long userId,
                                     @Param("date") LocalDate date,
                                     @Param("tag") String tag);

    WeightRecord selectLatestBefore(@Param("userId") Long userId,
                                    @Param("date") LocalDate date);

    int insert(WeightRecord weight);

    int updateByUserDateTag(WeightRecord weight);
}
