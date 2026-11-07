package com.autobeauty.controller;

import com.autobeauty.dto.ApiResponse;
import com.autobeauty.entity.PointRecord;
import com.autobeauty.entity.User;
import com.autobeauty.repository.AppointmentRepository;
import com.autobeauty.repository.PointRecordRepository;
import com.autobeauty.service.UserPrincipal;
import com.autobeauty.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PointRecordRepository pointRecordRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Optional<User> userOpt = userService.findById(userPrincipal.getId());
            
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("用户不存在", 404));
            }
            
            User user = userOpt.get();
            
            // 统计用户的预约数量
            long totalAppointments = appointmentRepository.countByUserId(user.getId());
            
            // 构建返回数据，包含用户信息和统计数据
            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("id", user.getId());
            userProfile.put("username", user.getUsername());
            userProfile.put("email", user.getEmail());
            userProfile.put("phone", user.getPhone());
            userProfile.put("realName", user.getRealName());
            userProfile.put("gender", user.getGender());
            userProfile.put("avatarUrl", user.getAvatarUrl());
            userProfile.put("address", user.getAddress());
            userProfile.put("points", user.getPoints());
            userProfile.put("status", user.getStatus() == 1 ? "ACTIVE" : "DISABLED");
            userProfile.put("role", user.getRole());
            userProfile.put("createdAt", user.getCreatedAt());
            userProfile.put("updatedAt", user.getUpdatedAt());
            userProfile.put("totalAppointments", totalAppointments);
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", userProfile));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取用户信息失败", 500));
        }
    }
    
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateUserProfile(@RequestBody User userUpdate, 
                                             Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Optional<User> userOpt = userService.findById(userPrincipal.getId());
            
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("用户不存在", 404));
            }
            
            User user = userOpt.get();
            
            // 更新允许用户修改的字段
            if (userUpdate.getRealName() != null) {
                user.setRealName(userUpdate.getRealName());
            }
            if (userUpdate.getEmail() != null) {
                user.setEmail(userUpdate.getEmail());
            }
            if (userUpdate.getPhone() != null) {
                user.setPhone(userUpdate.getPhone());
            }
            if (userUpdate.getAddress() != null) {
                user.setAddress(userUpdate.getAddress());
            }
            if (userUpdate.getGender() != null) {
                user.setGender(userUpdate.getGender());
            }
            if (userUpdate.getAvatarUrl() != null) {
                user.setAvatarUrl(userUpdate.getAvatarUrl());
            }
            
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新用户信息失败", 500));
        }
    }
    
    @GetMapping("/points")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserPoints(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Optional<User> userOpt = userService.findById(userPrincipal.getId());
            
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("用户不存在", 404));
            }
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", userOpt.get().getPoints()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取积分失败", 500));
        }
    }
    
    @PutMapping("/change-password")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData,
                                           Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");
            String confirmPassword = passwordData.get("confirmPassword");
            
            // 验证参数
            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("请输入当前密码", 400));
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("请输入新密码", 400));
            }
            
            if (confirmPassword == null || !confirmPassword.equals(newPassword)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("两次输入的新密码不一致", 400));
            }
            
            // 密码长度验证
            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("新密码长度不能少于6位", 400));
            }
            
            // 调用服务层修改密码
            userService.changePassword(userPrincipal.getId(), oldPassword, newPassword);
            
            return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("密码修改失败", 500));
        }
    }
    
    @GetMapping("/point-records")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getPointRecords(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            Pageable pageable = PageRequest.of(page, size);
            Page<PointRecord> pointRecords = pointRecordRepository.findByUserIdOrderByCreatedAtDesc(
                    userPrincipal.getId(), pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", pointRecords.getContent());
            response.put("totalElements", pointRecords.getTotalElements());
            response.put("totalPages", pointRecords.getTotalPages());
            response.put("currentPage", pointRecords.getNumber());
            response.put("pageSize", pointRecords.getSize());
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取积分记录失败", 500));
        }
    }
}
