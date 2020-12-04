package com.example.metrics.web;


import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
public class FileController {

    @Autowired
    private MultipartProperties multipartProperties;

    @PostMapping("/form/template/instance/file/upload")
    @ApiOperation(value = "文件上传")
    public String upload(HttpServletRequest request) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> multipartFileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entry : multipartFileMap.entrySet()) {
            MultipartFile file = entry.getValue();

            File dir = new File(multipartProperties.getLocation());
            String fileAbsolutePath = multipartProperties.getLocation() + File.separator + file.getOriginalFilename();

            if (!dir.exists()) {
                dir.mkdirs();
            }

            try {
                file.transferTo(new File(fileAbsolutePath));
            } catch (IOException e) {
                throw new RuntimeException("文件上传异常：" + e.getMessage());
            }
        }

        return "成功";
    }
}
