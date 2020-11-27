package com.example.metrics;

import java.math.BigDecimal;
import java.math.MathContext;

public class MetricsUtils {

    public static double calculateCpu(BigDecimal cpu) {
        return cpu.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double calculateBToG(BigDecimal memory) {
        return memory.round(MathContext.UNLIMITED)
                .divide(new BigDecimal(1024))
                .divide(new BigDecimal(1024))
                .divide(new BigDecimal(1024))
                .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double calculateKToG(BigDecimal memory) {
        return memory.round(MathContext.UNLIMITED)
                .divide(new BigDecimal(1024))
                .divide(new BigDecimal(1024))
                .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
