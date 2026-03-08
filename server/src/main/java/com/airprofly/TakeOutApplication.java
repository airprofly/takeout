package com.airprofly;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableScheduling // 启用定时任务
@EnableCaching  // 启用缓存
@Slf4j
public class TakeOutApplication {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}