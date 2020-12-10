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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

@Controller
public class DeploymentController {

    @PostMapping("/deployment")
    public ResponseEntity deployment() throws IOException {
        String deploymentYamlPath = "k8s/tomcat/tomcat-deployment.yaml";
        String serviceYamlPath = "k8s/tomcat/tomcat-service.yaml";
        String ingressYamlPath = "k8s/tomcat/tomcat-ingress.yaml";

        ApiClient client = MetricsUtils.connectK8s();
        AppsV1Api appsV1Api = new AppsV1Api();
        CoreV1Api coreV1Api = new CoreV1Api();
        ExtensionsV1beta1Api extensionsV1beta1Api = new ExtensionsV1beta1Api();

        // 部署 Deployment
        Object o = Yaml.load(getYamlFile(deploymentYamlPath));
        System.out.println(o.getClass());

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

    private Reader getYamlFile(String path) {
        Resource resource = new ClassPathResource(path);
        Reader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reader;
    }
}
