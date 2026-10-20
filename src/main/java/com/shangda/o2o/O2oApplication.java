package com.shangda.o2o;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 外卖订餐系统启动类
 */
@SpringBootApplication
@MapperScan("com.shangda.o2o.mapper")
public class O2oApplication {

    public static void main(String[] args) {
        SpringApplication.run(O2oApplication.class, args);
    }
}
