package com.example.metrics.web;

import com.example.metrics.MetricsUtils;
import io.kubernetes.client.Metrics;
import io.kubernetes.client.custom.NodeMetricsList;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MetricsController {

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/metrics")
    public ModelAndView metrics() {
        ModelAndView modelAndView = new ModelAndView();

        // 获取节点指标信息
        List<Map<String, Object>> nodeMetrics = nodeMetrics();

        // 获取POD指标信息
        List<Map<String, Object>> podMetrics = podMetrics();

        modelAndView.addObject("podMetrics", podMetrics);
        modelAndView.addObject("nodeMetrics", nodeMetrics);
        modelAndView.setViewName("metrics");
        return modelAndView;
    }

    private List<Map<String, Object>> nodeMetrics() {
        ApiClient client = connectK8s();

        Metrics metrics = new Metrics(client);
        NodeMetricsList nodeMetricsList = null;
        try {
            nodeMetricsList = metrics.getNodeMetrics();
        } catch (ApiException e) {
            e.printStackTrace();
        }

        // 获取已使用资源指标
        List<Map<String, Object>> usageList = new ArrayList<>();
        nodeMetricsList.getItems().stream().forEach(item -> {
            Map<String, Object> map = new HashMap<String, Object>() {{
                put("name", item.getMetadata().getName());
                put("cpu", MetricsUtils.calculateCpu(item.getUsage().get("cpu").getNumber()));
                put("memory", MetricsUtils.calculateBToG(item.getUsage().get("memory").getNumber()));
            }};
            usageList.add(map);
        });

        // 获取可分配的资源指标
        CoreV1Api api = new CoreV1Api();
        List<Map<String, Object>> allocatableList = new ArrayList<>();
        try {
            V1NodeList nodeList = api.listNode(null, null, null, null, null, null, null, null, null);
            nodeList.getItems().stream().forEach(item -> {
                Map<String, Object> map = new HashMap<String, Object>() {{
                    put("name", item.getMetadata().getName());
                    put("cpu", item.getStatus().getAllocatable().get("cpu").getNumber());
                    put("memory", MetricsUtils.calculateBToG(item.getStatus().getAllocatable().get("memory").getNumber()));
                }};
                allocatableList.add(map);
            });
        } catch (ApiException e) {
            e.printStackTrace();
        }

        // 合并
        usageList.stream().forEach(usageItem -> {
            allocatableList.stream().forEach(allocatableItem -> {
                if (usageItem.get("name").equals(allocatableItem.get("name"))) {
                    usageItem.put("cpu", usageItem.get("cpu") + "/" + allocatableItem.get("cpu"));
                    usageItem.put("memory", usageItem.get("memory") + "/" + allocatableItem.get("memory"));
                }
            });
        });

        return usageList;
    }

    private List<Map<String, Object>> podMetrics() {
        CoreV1Api api = new CoreV1Api(connectK8s());

        V1PodList podList = null;
        try {
            podList = api.listNamespacedPod("default", null, null, null, null, null, null, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return podList.getItems().stream().map(item -> new HashMap() {{
            put("name", item.getMetadata().getName());
            put("ready", item.getStatus().getContainerStatuses().get(0).getReady());
            put("address", item.getStatus().getHostIP());
            put("phase", item.getStatus().getPhase());
            put("started", item.getStatus().getContainerStatuses().get(0).getStarted());
            put("restartCount", item.getStatus().getContainerStatuses().get(0).getRestartCount());
        }}).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private ApiClient connectK8s() {
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
