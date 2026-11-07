package com.autobeauty.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Value("${file.upload.path}")
    private String uploadPath;
    
    @PostConstruct
    public void init() {
        System.out.println("WebMvcConfig initialized with uploadPath: " + uploadPath);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源访问路径
        // 获取项目根目录的绝对路径
        String projectRoot = System.getProperty("user.dir");
        
        // 清理路径，移除开头的 "./" 避免重复路径
        String cleanUploadPath = uploadPath.startsWith("./") ? uploadPath.substring(2) : uploadPath;
        String absolutePath = new File(projectRoot, cleanUploadPath).getAbsolutePath();
        
        // 转换为URL格式（使用正斜杠）
        String resourceLocation = "file:" + absolutePath.replace("\\", "/") + "/";
        
        System.out.println("=== Static Resource Configuration ===");
        System.out.println("Project root: " + projectRoot);
        System.out.println("Original upload path: " + uploadPath);
        System.out.println("Cleaned upload path: " + cleanUploadPath);
        System.out.println("Absolute path: " + absolutePath);
        System.out.println("Resource location: " + resourceLocation);
        System.out.println("Mapping: /uploads/** -> " + resourceLocation);
        System.out.println("=====================================");
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
    }
}
