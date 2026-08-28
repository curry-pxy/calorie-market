package com.calorie.market.mapper;

import com.calorie.market.domain.Record;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日记录 Mapper。
 */
public interface RecordMapper {

    List<Record> selectByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    List<Record> selectByUserRange(@Param("userId") Long userId,
                                   @Param("start") LocalDate start,
                                   @Param("end") LocalDate end);

    int insert(Record record);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);
}
