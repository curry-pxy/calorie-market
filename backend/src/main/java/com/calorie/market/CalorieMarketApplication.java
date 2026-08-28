package com.calorie.market;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.calorie.market.mapper")
@EnableScheduling
public class CalorieMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(CalorieMarketApplication.class, args);
    }
}
