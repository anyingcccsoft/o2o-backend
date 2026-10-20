package com.shangda.o2o.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单号生成器：yyyyMMddHHmmss + userId 后 4 位（不足补零） + 4 位随机数
 */
public final class OrderNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private OrderNoGenerator() {
    }

    public static String generate(Long userId) {
        String time = LocalDateTime.now().format(FORMATTER);
        String userSuffix = String.format("%04d", userId % 10000);
        String random = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        return time + userSuffix + random;
    }
}
