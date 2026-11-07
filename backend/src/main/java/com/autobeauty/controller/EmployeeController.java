package com.autobeauty.controller;

import com.autobeauty.dto.ApiResponse;
import com.autobeauty.entity.Appointment;
import com.autobeauty.entity.Employee;
import com.autobeauty.entity.User;
import com.autobeauty.repository.AppointmentRepository;
import com.autobeauty.repository.EmployeeRepository;
import com.autobeauty.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 获取当前员工信息
     */
    @GetMapping("/info")
    public ResponseEntity<?> getCurrentEmployeeInfo() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Optional<User> userOpt = userRepository.findByUsernameOrEmail(username);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("用户不存在", 400));
            }
            
            User user = userOpt.get();
            if (user.getEmployeeId() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("该账号未关联员工信息", 400));
            }
            
            Optional<Employee> employeeOpt = employeeRepository.findById(user.getEmployeeId());
            if (!employeeOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("员工信息不存在", 400));
            }
            
            Employee employee = employeeOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", employee.getId());
            response.put("name", employee.getName());
            response.put("phone", employee.getPhone());
            response.put("skills", employee.getSkills());
            response.put("workSchedule", employee.getWorkSchedule());
            response.put("avatarUrl", employee.getAvatarUrl());
            response.put("status", employee.getStatus());
            response.put("createdAt", employee.getCreatedAt());
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取失败: " + e.getMessage(), 400));
        }
    }
    
    /**
     * 获取员工的预约列表
     */
    @GetMapping("/appointments")
    public ResponseEntity<?> getEmployeeAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Optional<User> userOpt = userRepository.findByUsernameOrEmail(username);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("用户不存在", 400));
            }
            
            User user = userOpt.get();
            if (user.getEmployeeId() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("该账号未关联员工信息", 400));
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("appointmentTime").descending());
            Page<Appointment> appointments;
            
            if (status != null && !status.isEmpty()) {
                appointments = appointmentRepository.findByEmployeeIdAndStatus(user.getEmployeeId(), status, pageable);
            } else {
                appointments = appointmentRepository.findByEmployeeId(user.getEmployeeId(), pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", appointments));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取失败: " + e.getMessage(), 400));
        }
    }
    
    /**
     * 获取员工的今日预约
     */
    @GetMapping("/appointments/today")
    public ResponseEntity<?> getTodayAppointments() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Optional<User> userOpt = userRepository.findByUsernameOrEmail(username);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("用户不存在", 400));
            }
            
            User user = userOpt.get();
            if (user.getEmployeeId() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("该账号未关联员工信息", 400));
            }
            
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
            
            List<Appointment> appointments = appointmentRepository.findByEmployeeIdAndAppointmentTimeBetween(
                    user.getEmployeeId(), startOfDay, endOfDay);
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", appointments));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取失败: " + e.getMessage(), 400));
        }
    }
    
    /**
     * 获取员工的预约统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getAppointmentStatistics() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Optional<User> userOpt = userRepository.findByUsernameOrEmail(username);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("用户不存在", 400));
            }
            
            User user = userOpt.get();
            if (user.getEmployeeId() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("该账号未关联员工信息", 400));
            }
            
            Map<String, Object> statistics = new HashMap<>();
            
            // 总预约数
            long totalAppointments = appointmentRepository.countByEmployeeId(user.getEmployeeId());
            statistics.put("totalAppointments", totalAppointments);
            
            // 已完成预约数
            long completedAppointments = appointmentRepository.countByEmployeeIdAndStatus(user.getEmployeeId(), "COMPLETED");
            statistics.put("completedAppointments", completedAppointments);
            
            // 待处理预约数
            long pendingAppointments = appointmentRepository.countByEmployeeIdAndStatus(user.getEmployeeId(), "PENDING");
            statistics.put("pendingAppointments", pendingAppointments);
            
            // 已取消预约数
            long cancelledAppointments = appointmentRepository.countByEmployeeIdAndStatus(user.getEmployeeId(), "CANCELLED");
            statistics.put("cancelledAppointments", cancelledAppointments);
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", statistics));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取失败: " + e.getMessage(), 400));
        }
    }
    
    /**
     * 获取员工的仪表板数据
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Optional<User> userOpt = userRepository.findByUsernameOrEmail(username);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("用户不存在", 400));
            }
            
            User user = userOpt.get();
            if (user.getEmployeeId() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("该账号未关联员工信息", 400));
            }
            
            Map<String, Object> dashboard = new HashMap<>();
            
            // 员工基本信息
            Optional<Employee> employeeOpt = employeeRepository.findById(user.getEmployeeId());
            if (employeeOpt.isPresent()) {
                Employee employee = employeeOpt.get();
                dashboard.put("employeeInfo", employee);
            }
            
            // 今日预约
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
            List<Appointment> todayAppointments = appointmentRepository.findByEmployeeIdAndAppointmentTimeBetween(
                    user.getEmployeeId(), startOfDay, endOfDay);
            dashboard.put("todayAppointments", todayAppointments);
            
            // 统计数据
            long totalAppointments = appointmentRepository.countByEmployeeId(user.getEmployeeId());
            long completedAppointments = appointmentRepository.countByEmployeeIdAndStatus(user.getEmployeeId(), "COMPLETED");
            long pendingAppointments = appointmentRepository.countByEmployeeIdAndStatus(user.getEmployeeId(), "PENDING");
            
            dashboard.put("totalAppointments", totalAppointments);
            dashboard.put("completedAppointments", completedAppointments);
            dashboard.put("pendingAppointments", pendingAppointments);
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", dashboard));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取失败: " + e.getMessage(), 400));
        }
    }
    
    /**
     * 更新员工个人信息
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Optional<User> userOpt = userRepository.findByUsernameOrEmail(username);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("用户不存在", 400));
            }
            
            User user = userOpt.get();
            if (user.getEmployeeId() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("该账号未关联员工信息", 400));
            }
            
            Optional<Employee> employeeOpt = employeeRepository.findById(user.getEmployeeId());
            if (!employeeOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("员工信息不存在", 400));
            }
            
            Employee employee = employeeOpt.get();
            
            // 更新允许修改的字段
            if (request.containsKey("phone")) {
                employee.setPhone((String) request.get("phone"));
            }
            if (request.containsKey("skills")) {
                employee.setSkills((String) request.get("skills"));
            }
            if (request.containsKey("workSchedule")) {
                employee.setWorkSchedule((String) request.get("workSchedule"));
            }
            
            employeeRepository.save(employee);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", employee.getId());
            response.put("name", employee.getName());
            response.put("phone", employee.getPhone());
            response.put("skills", employee.getSkills());
            response.put("workSchedule", employee.getWorkSchedule());
            response.put("avatarUrl", employee.getAvatarUrl());
            response.put("status", employee.getStatus());
            response.put("createdAt", employee.getCreatedAt());
            
            return ResponseEntity.ok(ApiResponse.success("更新成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新失败: " + e.getMessage(), 500));
        }
    }
    
    /**
     * 上传员工头像
     */
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Optional<User> userOpt = userRepository.findByUsernameOrEmail(username);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("用户不存在", 400));
            }
            
            User user = userOpt.get();
            if (user.getEmployeeId() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("该账号未关联员工信息", 400));
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("只能上传图片文件", 400));
            }
            
            // 验证文件大小（限制为 5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("文件大小不能超过 5MB", 400));
            }
            
            Optional<Employee> employeeOpt = employeeRepository.findById(user.getEmployeeId());
            if (!employeeOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("员工信息不存在", 400));
            }
            
            Employee employee = employeeOpt.get();
            
            // 保存头像数据
            employee.setAvatarData(file.getBytes());
            // 生成 Data URL 用于前端显示
            String dataUrl = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(file.getBytes());
            employee.setAvatarUrl(dataUrl);
            
            employeeRepository.save(employee);
            
            Map<String, Object> response = new HashMap<>();
            response.put("avatarUrl", dataUrl);
            response.put("message", "头像上传成功");
            
            return ResponseEntity.ok(ApiResponse.success("上传成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("上传失败: " + e.getMessage(), 500));
        }
    }
}
