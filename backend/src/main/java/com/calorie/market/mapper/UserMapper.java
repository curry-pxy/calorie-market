package com.calorie.market.mapper;

import com.calorie.market.domain.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 Mapper。
 */
public interface UserMapper {

    List<Long> selectAllIds();

    User selectByOpenid(String openid);

    User selectByPhone(String phone);

    User selectById(Long id);

    int insert(User user);

    int updateProfile(User user);

    int deleteById(@Param("id") Long id);
}
