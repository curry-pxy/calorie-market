package com.calorie.market.mapper;

import com.calorie.market.domain.Sector;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 板块 Mapper。
 */
public interface SectorMapper {

    List<Sector> selectByUser(Long userId);

    Sector selectByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    int insert(Sector sector);

    int insertBatch(List<Sector> sectors);

    int softDelete(@Param("id") Long id, @Param("userId") Long userId);
}
