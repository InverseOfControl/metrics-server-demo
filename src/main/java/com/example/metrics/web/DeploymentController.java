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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

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
        V1Deployment deploymentBody = Yaml.loadAs(getYamlFile(deploymentYamlPath), V1Deployment.class);

        System.out.println("apiVersion:" + deploymentBody.getApiVersion());
        System.out.println("apiVersion:" + deploymentBody.getKind());

        try {
            appsV1Api.createNamespacedDeployment("default", deploymentBody, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        // 部署 Service
        V1Service serviceBody = Yaml.loadAs(getYamlFile(serviceYamlPath), V1Service.class);
        try {
            coreV1Api.createNamespacedService("default", serviceBody, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        // 部署 Ingress
        ExtensionsV1beta1Ingress ingressBody = Yaml.loadAs(getYamlFile(ingressYamlPath), ExtensionsV1beta1Ingress.class);
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

    private Reader getYamlFile(String path) {
        Resource resource = new ClassPathResource(path);
        BufferedInputStream in;
        try {
            return new InputStreamReader(new BufferedInputStream(resource.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
