package com.calorie.market.service;

import com.calorie.market.domain.User;
import com.calorie.market.dto.ProfileRequest;
import com.calorie.market.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户资料服务。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public User getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    public User updateProfile(Long userId, ProfileRequest request) {
        if (request.getAge() == null || request.getAge() < 10 || request.getAge() > 100) {
            throw new IllegalArgumentException("年龄需在 10-100 之间");
        }
        if (request.getHeight() == null || request.getHeight() < 100 || request.getHeight() > 230) {
            throw new IllegalArgumentException("身高需在 100-230 cm 之间");
        }
        if (request.getWeight() == null || request.getWeight() < 30 || request.getWeight() > 200) {
            throw new IllegalArgumentException("体重需在 30-200 kg 之间");
        }
        if (request.getTarget() == null || request.getTarget() < 100 || request.getTarget() > 1500) {
            throw new IllegalArgumentException("目标赤字需在 100-1500 kcal 之间");
        }
        String gender = StringUtils.hasText(request.getGender()) ? request.getGender() : "男";
        if (!"男".equals(gender) && !"女".equals(gender)) {
            throw new IllegalArgumentException("性别只能是男或女");
        }
        User user = new User();
        user.setId(userId);
        user.setGender(gender);
        user.setAge(request.getAge());
        user.setHeight(request.getHeight());
        user.setWeight(request.getWeight());
        user.setTarget(request.getTarget());
        user.setBmr(calcBmr(gender, request.getAge(), request.getHeight(), request.getWeight()));
        userMapper.updateProfile(user);
        return getProfile(userId);
    }

    /**
     * Mifflin-St Jeor 公式估算基础代谢。
     */
    public static int calcBmr(String gender, int age, int height, double weight) {
        double base = 10 * weight + 6.25 * height - 5 * age;
        return (int) Math.round("男".equals(gender) ? base + 5 : base - 161);
    }
}
