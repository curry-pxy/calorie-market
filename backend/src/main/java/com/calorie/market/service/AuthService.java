package com.calorie.market.service;

import com.calorie.market.domain.Sector;
import com.calorie.market.domain.User;
import com.calorie.market.common.PasswordUtil;
import com.calorie.market.dto.LoginResponse;
import com.calorie.market.mapper.SectorMapper;
import com.calorie.market.mapper.UserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录服务。未配置微信 appid/secret 时走本地开发模式。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final SectorMapper sectorMapper;
    private final ObjectMapper objectMapper;

    @Value("${wx.appid:}")
    private String wxAppid;

    @Value("${wx.secret:}")
    private String wxSecret;

    public LoginResponse login(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("登录 code 不能为空");
        }
        boolean useWxLogin = StringUtils.hasText(wxAppid) && StringUtils.hasText(wxSecret);
        String openid = useWxLogin ? wxCode2Session(code.trim()) : "dev-" + code.trim();
        User user = userMapper.selectByOpenid(openid);
        boolean isNew = false;
        if (user == null) {
            user = buildDefaultUser(openid);
            userMapper.insert(user);
            insertDefaultSectors(user.getId());
            isNew = true;
        }
        return new LoginResponse(user.getId(), openid, user.getPhone(), isNew);
    }

    public LoginResponse registerByPhone(String phone, String password) {
        String normalized = normalizePhone(phone);
        validatePassword(password);
        if (userMapper.selectByPhone(normalized) != null) {
            throw new IllegalArgumentException("该手机号已注册，请直接登录");
        }
        User user = buildPhoneUser(normalized, password);
        userMapper.insert(user);
        insertDefaultSectors(user.getId());
        return new LoginResponse(user.getId(), user.getOpenid(), normalized, true);
    }

    public LoginResponse loginByPhone(String phone, String password) {
        String normalized = normalizePhone(phone);
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("请输入密码");
        }
        User user = userMapper.selectByPhone(normalized);
        if (user == null) {
            throw new IllegalArgumentException("该手机号未注册");
        }
        if (!PasswordUtil.verify(password, user.getPassword())) {
            throw new IllegalArgumentException("手机号或密码错误");
        }
        return new LoginResponse(user.getId(), user.getOpenid(), normalized, false);
    }

    private User buildDefaultUser(String openid) {
        User user = new User();
        user.setOpenid(openid);
        user.setNickname("新用户");
        user.setGender("男");
        user.setAge(28);
        user.setHeight(175);
        user.setWeight(63.0);
        user.setTarget(400);
        user.setBmr(UserService.calcBmr("男", 28, 175, 63.0));
        return user;
    }

    private User buildPhoneUser(String phone, String password) {
        User user = new User();
        user.setPhone(phone);
        user.setPassword(PasswordUtil.hash(password));
        user.setNickname("用户" + phone.substring(phone.length() - 4));
        user.setGender("男");
        user.setAge(28);
        user.setHeight(175);
        user.setWeight(63.0);
        user.setTarget(400);
        user.setBmr(UserService.calcBmr("男", 28, 175, 63.0));
        return user;
    }

    private String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone) || !phone.trim().matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("请输入正确的 11 位手机号");
        }
        return phone.trim();
    }

    private void validatePassword(String password) {
        if (!StringUtils.hasText(password) || password.length() < 6 || password.length() > 20) {
            throw new IllegalArgumentException("密码长度需为 6-20 位");
        }
    }

    private void insertDefaultSectors(Long userId) {
        List<Sector> sectors = new ArrayList<Sector>();
        String[][] defaults = {
                {"早餐", "in"}, {"午餐", "in"}, {"晚餐", "in"}, {"加餐", "in"},
                {"高能饮品", "in"}, {"运动", "out"}, {"基础代谢", "base"}
        };
        for (int i = 0; i < defaults.length; i++) {
            Sector sector = new Sector();
            sector.setUserId(userId);
            sector.setName(defaults[i][0]);
            sector.setType(defaults[i][1]);
            sector.setCustom(Boolean.FALSE);
            sector.setSortOrder(i + 1);
            sectors.add(sector);
        }
        sectorMapper.insertBatch(sectors);
    }

    private String wxCode2Session(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + wxAppid
                + "&secret=" + wxSecret + "&js_code=" + code + "&grant_type=authorization_code";
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");
            int status = conn.getResponseCode();
            InputStream is = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
            String body = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            JsonNode node = objectMapper.readTree(body);
            if (node.has("errcode") && node.get("errcode").asInt() != 0) {
                throw new IllegalArgumentException("微信登录失败：" + node.path("errmsg").asText());
            }
            String openid = node.path("openid").asText();
            if (!StringUtils.hasText(openid)) {
                throw new IllegalArgumentException("微信登录失败：未返回 openid");
            }
            return openid;
        } catch (IOException e) {
            throw new IllegalStateException("调用微信登录接口失败", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
