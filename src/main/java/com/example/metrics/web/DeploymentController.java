package com.example.metrics.web;

import com.example.metrics.utils.MetricsUtils;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1Ingress;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.util.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller
public class DeploymentController {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentController.class);

    @PostMapping("/deployment")
    public ResponseEntity deployment() throws IOException {
        logger.info("部署开始");

        String deploymentYamlPath = "tomcat-deployment.yaml";
        String serviceYamlPath = "k8s/tomcat/tomcat-service.yaml";
        String ingressYamlPath = "k8s/tomcat/tomcat-ingress.yaml";

        ApiClient client = MetricsUtils.connectK8s();
        AppsV1Api appsV1Api = new AppsV1Api();
        CoreV1Api coreV1Api = new CoreV1Api();
        ExtensionsV1beta1Api extensionsV1beta1Api = new ExtensionsV1beta1Api();

        // 部署 Deployment
        String content = getYamlFile(deploymentYamlPath);
        logger.info("content : " + content);

        Object o = Yaml.load(content);
        logger.info("object : " + o.getClass());

        V1Deployment deploymentBody = (V1Deployment) Yaml.load(getYamlFile(deploymentYamlPath));

        System.out.println("apiVersion:" + deploymentBody.getApiVersion());
        System.out.println("apiVersion:" + deploymentBody.getKind());

        try {
            appsV1Api.createNamespacedDeployment("default", deploymentBody, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        // 部署 Service
        V1Service serviceBody = (V1Service) Yaml.load(getYamlFile(serviceYamlPath));
        try {
            coreV1Api.createNamespacedService("default", serviceBody, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        // 部署 Ingress
        ExtensionsV1beta1Ingress ingressBody = (ExtensionsV1beta1Ingress) Yaml.load(getYamlFile(ingressYamlPath));
        try {
            extensionsV1beta1Api.createNamespacedIngress("default", ingressBody, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/deployment/delete")
    public ResponseEntity delete() throws IOException {
        ApiClient client = MetricsUtils.connectK8s();
        AppsV1Api appsV1Api = new AppsV1Api(client);
        CoreV1Api coreV1Api = new CoreV1Api(client);
        ExtensionsV1beta1Api extensionsV1beta1Api = new ExtensionsV1beta1Api(client);

        // 删除 Deployment
        try {
            appsV1Api.deleteNamespacedDeployment("tomcat-deployment", "default", null, null, null, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        // 删除 Service
        try {
            coreV1Api.deleteNamespacedService("tomcat-service", "default", null, null, null, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        // 删除 Ingress
        try {
            extensionsV1beta1Api.deleteNamespacedIngress("tomcat-ingress", "default", null, null, null, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok().build();
    }

    private String getYamlFile(String path) {
        Resource resource = new ClassPathResource(path);
        BufferedInputStream in;
        try {
            in = new BufferedInputStream(resource.getInputStream());
            ByteArrayOutputStream out =
                    new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];
            int n;
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
            in.close();
            out.flush();
            buffer = out.toByteArray();
            return new String(buffer, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
