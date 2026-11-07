package com.autobeauty.controller;

import com.autobeauty.dto.ApiResponse;
import com.autobeauty.entity.Employee;
import com.autobeauty.entity.Service;
import com.autobeauty.entity.User;
import com.autobeauty.entity.Vehicle;
import com.autobeauty.repository.EmployeeRepository;
import com.autobeauty.repository.ServiceRepository;
import com.autobeauty.repository.UserRepository;
import com.autobeauty.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/upload")
public class FileUploadController {
    
    @Value("${file.upload.path}")
    private String uploadPath;
    
    @Value("${file.upload.avatar-path}")
    private String avatarPath;
    
    @Value("${file.upload.vehicle-path}")
    private String vehiclePath;
    
    @Value("${file.upload.service-path}")
    private String servicePath;
    
    @Value("${file.upload.review-path}")
    private String reviewPath;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    /**
     * 上传头像（返回Base64数据，由前端保存）
     */
    @PostMapping("/avatar")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("文件不能为空", 400));
            }
            
            if (!isImageFile(file)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("只支持图片文件", 400));
            }
            
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("文件大小不能超过5MB", 400));
            }
            
            // 返回Base64编码的图片数据
            byte[] imageBytes = file.getBytes();
            String base64Data = java.util.Base64.getEncoder().encodeToString(imageBytes);
            String contentType = file.getContentType();
            String dataUrl = "data:" + contentType + ";base64," + base64Data;
            
            return ResponseEntity.ok(ApiResponse.success("头像上传成功", dataUrl));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("文件上传失败", 500));
        }
    }
    
    /**
     * 上传车辆图片（返回Base64数据）
     */
    @PostMapping("/vehicle")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadVehicleImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("文件不能为空", 400));
            }
            
            if (!isImageFile(file)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("只支持图片文件", 400));
            }
            
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("文件大小不能超过5MB", 400));
            }
            
            // 返回Base64编码的图片数据
            byte[] imageBytes = file.getBytes();
            String base64Data = java.util.Base64.getEncoder().encodeToString(imageBytes);
            String contentType = file.getContentType();
            String dataUrl = "data:" + contentType + ";base64," + base64Data;
            
            return ResponseEntity.ok(ApiResponse.success("车辆图片上传成功", dataUrl));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("文件上传失败", 500));
        }
    }
    
    /**
     * 上传服务图片（返回Base64数据）
     */
    @PostMapping("/service")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadServiceImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("文件不能为空", 400));
            }
            
            if (!isImageFile(file)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("只支持图片文件", 400));
            }
            
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("文件大小不能超过5MB", 400));
            }
            
            // 返回Base64编码的图片数据
            byte[] imageBytes = file.getBytes();
            String base64Data = java.util.Base64.getEncoder().encodeToString(imageBytes);
            String contentType = file.getContentType();
            String dataUrl = "data:" + contentType + ";base64," + base64Data;
            
            return ResponseEntity.ok(ApiResponse.success("服务图片上传成功", dataUrl));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("文件上传失败", 500));
        }
    }
    
    @PostMapping("/review")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadReviewImage(@RequestParam("file") MultipartFile file) {
        // 评价图片暂时保留文件系统存储（因为可能有多张）
        return uploadFileToSystem(file, reviewPath, "评价图片");
    }
    
    /**
     * 检查是否为图片文件
     */
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
    
    /**
     * 上传文件到文件系统（用于评价图片等多图场景）
     */
    private ResponseEntity<?> uploadFileToSystem(MultipartFile file, String targetPath, String fileType) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("文件不能为空", 400));
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("只支持图片文件", 400));
        }
        
        // 检查文件大小（10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("文件大小不能超过10MB", 400));
        }
        
        try {
            // 创建目录
            File directory = new File(targetPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            String filename = timestamp + "_" + uuid + extension;
            
            // 保存文件
            Path filePath = Paths.get(targetPath, filename);
            Files.copy(file.getInputStream(), filePath);
            
            // 返回文件URL
            // 从targetPath中提取子目录名（如avatars, vehicles等）
            String subDir = targetPath.replace("./uploads/", "").replace("/", "");
            String fileUrl = "/uploads/" + subDir + "/" + filename;
            
            return ResponseEntity.ok(ApiResponse.success(fileType + "上传成功", fileUrl));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("文件上传失败", 500));
        }
    }
}
