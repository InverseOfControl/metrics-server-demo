package com.example.metrics.utils;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 * 工具类
 *
 * @author 高宏旭
 * @date 2020/12/8 0008 14:51
 */
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

    public static ApiClient connectK8s() {
        Resource resource = new ClassPathResource("kubectl.config");
        Reader reader;
        ApiClient client = null;
        try {
            reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            KubeConfig config = KubeConfig.loadKubeConfig(reader);
            client = ClientBuilder.kubeconfig(config).build();
            Configuration.setDefaultApiClient(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Configuration.setDefaultApiClient(client);
        return client;
    }
}
