package com.bnbms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan("com.bnbms.mapper")
@SpringBootApplication
public class BnbmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BnbmsApplication.class, args);
        System.out.println("===== 民宿管理系统后端启动成功 http://localhost:8080 =====");
    }
}
