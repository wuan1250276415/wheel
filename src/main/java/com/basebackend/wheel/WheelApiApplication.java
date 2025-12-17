package com.basebackend.wheel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 情侣转盘API启动类
 *
 * @author wheel-api
 */
@SpringBootApplication(scanBasePackages = {
        "com.basebackend.wheel",
        "com.basebackend.common",
        "com.basebackend.security",
        "com.basebackend.jwt",
        "com.basebackend.database",
        "com.basebackend.cache",
        "com.basebackend.logging",
        "com.basebackend.observability",
        "com.basebackend.backup",
        "com.basebackend.feign",
        "com.basebackend.messaging"
})
@MapperScan({
        "com.basebackend.wheel.mapper",
        "com.basebackend.database.**.mapper",
        "com.basebackend.backup.**.mapper",
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.basebackend.feign.client"})
@EnableAspectJAutoProxy
public class WheelApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WheelApiApplication.class, args);
    }
}
