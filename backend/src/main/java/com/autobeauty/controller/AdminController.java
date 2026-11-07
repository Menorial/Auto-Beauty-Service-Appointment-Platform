package com.autobeauty.controller;

import com.autobeauty.dto.ApiResponse;
import com.autobeauty.dto.CreateEmployeeAccountRequest;
import com.autobeauty.entity.Employee;
import com.autobeauty.entity.Review;
import com.autobeauty.entity.Service;
import com.autobeauty.entity.SystemConfig;
import com.autobeauty.entity.User;
import com.autobeauty.repository.UserRepository;
import com.autobeauty.repository.EmployeeRepository;
import com.autobeauty.service.AdminService;
import com.autobeauty.service.ReviewService;
import com.autobeauty.service.ServiceService;
import com.autobeauty.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private ServiceService serviceService;
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取管理员统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getAdminStats() {
        try {
            return ResponseEntity.ok(ApiResponse.success("获取成功", adminService.getAdminStats()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取统计数据失败", 500));
        }
    }

    /**
     * 获取最近预约
     */
    @GetMapping("/appointments/recent")
    public ResponseEntity<?> getRecentAppointments(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok(ApiResponse.success("获取成功", 
                adminService.getRecentAppointments(pageable)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取最近预约失败", 500));
        }
    }
    
    /**
     * 获取预约趋势数据
     */
    @GetMapping("/appointments/trend")
    public ResponseEntity<?> getAppointmentTrend(@RequestParam(defaultValue = "7") int days) {
        try {
            return ResponseEntity.ok(ApiResponse.success("获取成功", 
                adminService.getAppointmentTrend(days)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取预约趋势失败", 500));
        }
    }

    /**
     * 获取所有预约（管理员，支持搜索）
     */
    @GetMapping("/appointments")
    public ResponseEntity<?> getAllAppointments(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(required = false) String orderNo,
                                              @RequestParam(required = false) String username,
                                              @RequestParam(required = false) Long serviceId,
                                              @RequestParam(required = false) String paymentStatus) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            // 检查是否有非空的搜索条件
            boolean hasSearchConditions = (orderNo != null && !orderNo.trim().isEmpty()) ||
                                         (username != null && !username.trim().isEmpty()) ||
                                         (serviceId != null) ||
                                         (paymentStatus != null && !paymentStatus.trim().isEmpty());
            
            // 如果有搜索条件，使用搜索方法
            if (hasSearchConditions) {
                return ResponseEntity.ok(ApiResponse.success("获取成功", 
                    adminService.searchAppointments(orderNo, username, serviceId, status, paymentStatus, pageable)));
            } else {
                return ResponseEntity.ok(ApiResponse.success("获取成功", 
                    adminService.getAllAppointments(pageable, status)));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取预约列表失败", 500));
        }
    }

    /**
     * 根据日期范围获取预约
     */
    @GetMapping("/appointments/date-range")
    public ResponseEntity<?> getAppointmentsByDateRange(@RequestParam String startDate,
                                                       @RequestParam String endDate,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok(ApiResponse.success("获取成功", 
                adminService.getAppointmentsByDateRange(startDate, endDate, pageable)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取日期范围预约失败", 500));
        }
    }

    /**
     * 更新预约状态
     */
    @PutMapping("/appointments/{id}/status")
    public ResponseEntity<?> updateAppointmentStatus(@PathVariable Long id, 
                                                   @RequestBody UpdateStatusRequest request) {
        try {
            adminService.updateAppointmentStatus(id, request.getStatus());
            return ResponseEntity.ok(ApiResponse.success("状态更新成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("状态更新失败", 500));
        }
    }

    /**
     * 分配员工
     */
    @PutMapping("/appointments/{appointmentId}/assign")
    public ResponseEntity<?> assignEmployee(@PathVariable Long appointmentId,
                                          @RequestBody AssignEmployeeRequest request) {
        try {
            adminService.assignEmployee(appointmentId, request.getEmployeeId());
            return ResponseEntity.ok(ApiResponse.success("员工分配成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("员工分配失败", 500));
        }
    }

    /**
     * 获取所有用户（支持搜索）
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(required = false) String username,
                                       @RequestParam(required = false) String realName,
                                       @RequestParam(required = false) String phone,
                                       @RequestParam(required = false) String status) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            // 检查是否有非空的搜索条件
            boolean hasSearchConditions = (username != null && !username.trim().isEmpty()) ||
                                         (realName != null && !realName.trim().isEmpty()) ||
                                         (phone != null && !phone.trim().isEmpty()) ||
                                         (status != null && !status.trim().isEmpty());
            
            // 如果有搜索条件，使用搜索方法
            if (hasSearchConditions) {
                return ResponseEntity.ok(ApiResponse.success("获取成功", 
                    adminService.searchUsers(username, realName, phone, status, pageable)));
            } else {
                return ResponseEntity.ok(ApiResponse.success("获取成功", 
                    adminService.getAllUsers(pageable)));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取用户列表失败", 500));
        }
    }

    /**
     * 获取所有员工（支持搜索）
     */
    @GetMapping("/employees")
    public ResponseEntity<?> getAllEmployees(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) String name,
                                           @RequestParam(required = false) String phone,
                                           @RequestParam(required = false) String skills) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            // 检查是否有非空的搜索条件
            boolean hasSearchConditions = (name != null && !name.trim().isEmpty()) ||
                                         (phone != null && !phone.trim().isEmpty()) ||
                                         (skills != null && !skills.trim().isEmpty());
            
            // 如果有搜索条件，使用搜索方法
            if (hasSearchConditions) {
                return ResponseEntity.ok(ApiResponse.success("获取成功", 
                    adminService.searchEmployees(name, phone, skills, pageable)));
            } else {
                return ResponseEntity.ok(ApiResponse.success("获取成功", 
                    adminService.getAllEmployees(pageable)));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取员工列表失败", 500));
        }
    }

    /**
     * 获取所有服务（管理员，支持搜索）
     */
    @GetMapping("/services")
    public ResponseEntity<?> getAllServices(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) String name,
                                          @RequestParam(required = false) String category,
                                          @RequestParam(required = false) String status) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            // 检查是否有非空的搜索条件
            boolean hasSearchConditions = (name != null && !name.trim().isEmpty()) ||
                                         (category != null && !category.trim().isEmpty()) ||
                                         (status != null && !status.trim().isEmpty());
            
            // 如果有搜索条件，使用搜索方法
            if (hasSearchConditions) {
                return ResponseEntity.ok(ApiResponse.success("获取成功", 
                    adminService.searchServices(name, category, status, pageable)));
            } else {
                return ResponseEntity.ok(ApiResponse.success("获取成功", 
                    adminService.getAllServicesForAdmin(pageable)));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取服务列表失败", 500));
        }
    }

    /**
     * 创建服务
     */
    @PostMapping("/services")
    public ResponseEntity<?> createService(@RequestBody Service service) {
        try {
            Service createdService = serviceService.createService(service);
            return ResponseEntity.ok(ApiResponse.success("创建成功", createdService));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建服务失败", 500));
        }
    }

    /**
     * 更新服务
     */
    @PutMapping("/services/{id}")
    public ResponseEntity<?> updateService(@PathVariable Long id, @RequestBody Service service) {
        try {
            // 确保使用路径参数中的ID
            service.setId(id);
            Service updatedService = serviceService.updateService(service);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updatedService));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新服务失败", 500));
        }
    }

    /**
     * 删除服务
     */
    @DeleteMapping("/services/{id}")
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        try {
            serviceService.deleteService(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除服务失败", 500));
        }
    }

    // 员工管理接口
    @PostMapping("/employees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createEmployee(@RequestBody Employee employee) {
        try {
            System.out.println("AdminController.createEmployee called - 创建员工");
            Employee savedEmployee = adminService.createEmployee(employee);
            return ResponseEntity.ok(ApiResponse.success("员工创建成功", savedEmployee));
        } catch (Exception e) {
            System.out.println("AdminController.createEmployee error: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建员工失败: " + e.getMessage(), 500));
        }
    }

    @PutMapping("/employees/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        try {
            System.out.println("AdminController.updateEmployee called - 更新员工ID: " + id);
            Employee updatedEmployee = adminService.updateEmployee(id, employee);
            return ResponseEntity.ok(ApiResponse.success("员工更新成功", updatedEmployee));
        } catch (Exception e) {
            System.out.println("AdminController.updateEmployee error: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新员工失败: " + e.getMessage(), 500));
        }
    }

    @DeleteMapping("/employees/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        try {
            System.out.println("AdminController.deleteEmployee called - 删除员工ID: " + id);
            adminService.deleteEmployee(id);
            return ResponseEntity.ok(ApiResponse.success("员工删除成功", null));
        } catch (Exception e) {
            System.out.println("AdminController.deleteEmployee error: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除员工失败: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/employees/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        try {
            System.out.println("AdminController.getEmployeeById called - 获取员工ID: " + id);
            Employee employee = adminService.getEmployeeById(id);
            return ResponseEntity.ok(ApiResponse.success("获取员工信息成功", employee));
        } catch (Exception e) {
            System.out.println("AdminController.getEmployeeById error: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取员工信息失败: " + e.getMessage(), 500));
        }
    }

    /**
     * 为员工创建用户账号
     */
    @PostMapping("/employees/{employeeId}/create-account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createEmployeeAccount(@PathVariable Long employeeId, 
                                                   @RequestBody CreateEmployeeAccountRequest request) {
        try {
            // 检查员工是否存在
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (!employeeOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("员工不存在", 400));
            }
            
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("用户名已存在", 400));
            }
            
            // 检查邮箱是否已存在
            String email = request.getEmail();
            if (email != null && email.trim().isEmpty()) {
                email = null;
            }
            if (email != null && userRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("邮箱已被注册", 400));
            }
            
            // 创建用户账号
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(email);
            user.setRealName(employeeOpt.get().getName());
            user.setRole(User.Role.EMPLOYEE);
            user.setEmployeeId(employeeId);
            user.setStatus(1);
            
            User savedUser = userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedUser.getId());
            response.put("username", savedUser.getUsername());
            response.put("email", savedUser.getEmail());
            response.put("realName", savedUser.getRealName());
            response.put("role", savedUser.getRole());
            response.put("employeeId", savedUser.getEmployeeId());
            
            return ResponseEntity.ok(ApiResponse.success("员工账号创建成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建员工账号失败: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Map<String, Object> userDetail = adminService.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success("获取成功", userDetail));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取用户详情失败", 500));
        }
    }
    
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> userData) {
        try {
            adminService.updateUser(id, userData);
            return ResponseEntity.ok(ApiResponse.success("用户信息更新成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新用户信息失败", 500));
        }
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            if (status == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("状态参数不能为空", 400));
            }
            
            adminService.updateUserStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("用户状态更新成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新用户状态失败", 500));
        }
    }

    @PutMapping("/users/{id}/password")
    public ResponseEntity<?> resetUserPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            
            // 调用服务层重置密码
            String newPassword = adminService.resetUserPassword(id, password);
            
            // 返回新密码（如果是自动生成的）
            Map<String, String> result = new HashMap<>();
            result.put("password", newPassword);
            
            return ResponseEntity.ok(ApiResponse.success("密码重置成功", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("重置密码失败", 500));
        }
    }

    @PutMapping("/users/{id}/points")
    public ResponseEntity<?> updateUserPoints(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String type = (String) request.get("type");
            Integer amount = (Integer) request.get("amount");
            String reason = (String) request.get("reason");
            
            if (type == null || amount == null || reason == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("参数不完整", 400));
            }
            
            adminService.updateUserPoints(id, type, amount, reason);
            return ResponseEntity.ok(ApiResponse.success("积分更新成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新用户积分失败", 500));
        }
    }

    @GetMapping("/reviews/test")
    public ResponseEntity<?> testReviewsEndpoint() {
        System.out.println("TEST ENDPOINT CALLED - 测试接口被调用了");
        return ResponseEntity.ok(ApiResponse.success("Reviews endpoint is working", "测试成功"));
    }

    @GetMapping("/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllReviews(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(required = false) String username,
                                         @RequestParam(required = false) Long serviceId,
                                         @RequestParam(required = false) Integer rating,
                                         @RequestParam(required = false) String replyStatus) {
        try {
            System.out.println("AdminController.getAllReviews called - 接口已被访问");
            Pageable pageable = PageRequest.of(page, size);
            Page<com.autobeauty.dto.ReviewResponse> reviews;
            
            // 检查是否有非空的搜索条件
            boolean hasSearchConditions = (username != null && !username.trim().isEmpty()) ||
                                         (serviceId != null) ||
                                         (rating != null) ||
                                         (replyStatus != null && !replyStatus.trim().isEmpty());
            
            // 如果有搜索条件，使用搜索方法，否则获取所有评价
            if (hasSearchConditions) {
                reviews = reviewService.searchReviews(username, serviceId, rating, replyStatus, pageable);
            } else {
                reviews = reviewService.getAllReviews(pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", reviews));
        } catch (Exception e) {
            System.out.println("AdminController.getAllReviews error: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取评价列表失败: " + e.getMessage(), 500));
        }
    }

    @PostMapping("/reviews/{id}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> replyReview(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String reply = request.get("reply");
            if (reply == null || reply.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("回复内容不能为空", 400));
            }
            
            Review updatedReview = reviewService.replyToReview(id, reply);
            return ResponseEntity.ok(ApiResponse.success("回复成功", updatedReview));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("回复失败: " + e.getMessage(), 500));
        }
    }

    @DeleteMapping("/reviews/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除失败: " + e.getMessage(), 500));
        }
    }

    // 数据报表相关接口
    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReports(@RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate) {
        try {
            // 获取基础统计数据
            Map<String, Object> stats = adminService.getAdminStats();
            
            // 构建报表数据结构
            Map<String, Object> reportData = new HashMap<>();
            
            // 概览数据 - 使用真实数据
            Map<String, Object> overview = new HashMap<>();
            overview.put("totalRevenue", stats.get("totalRevenue"));
            overview.put("revenueGrowth", 0);
            overview.put("totalOrders", stats.get("totalAppointments"));
            overview.put("orderGrowth", 0);
            overview.put("totalUsers", stats.get("totalUsers"));
            overview.put("userGrowth", 0);
            overview.put("averageRating", stats.get("averageRating")); // 使用真实平均评分
            overview.put("ratingChange", 0);
            
            // 添加其他统计数据
            overview.put("activeUsers", stats.get("activeUsers"));
            overview.put("todayNewUsers", stats.get("todayNewUsers"));
            overview.put("totalPoints", stats.get("totalPoints"));
            overview.put("totalReviews", stats.get("totalReviews"));
            overview.put("pendingReviews", stats.get("pendingReviews"));
            overview.put("todayNewReviews", stats.get("todayNewReviews"));
            overview.put("todayNewAppointments", stats.get("todayNewAppointments"));
            overview.put("pendingAppointments", stats.get("pendingAppointments"));
            overview.put("confirmedAppointments", stats.get("confirmedAppointments"));
            overview.put("inProgressAppointments", stats.get("inProgressAppointments"));
            overview.put("completedAppointments", stats.get("completedAppointments"));
            
            reportData.put("overview", overview);
            
            // 订单状态分布数据
            List<Map<String, Object>> orderStatusData = new ArrayList<>();
            Map<String, Object> pending = new HashMap<>();
            pending.put("name", "待确认");
            pending.put("value", stats.get("pendingAppointments"));
            orderStatusData.add(pending);
            
            Map<String, Object> confirmed = new HashMap<>();
            confirmed.put("name", "已确认");
            confirmed.put("value", stats.get("confirmedAppointments"));
            orderStatusData.add(confirmed);
            
            Map<String, Object> inProgress = new HashMap<>();
            inProgress.put("name", "进行中");
            inProgress.put("value", stats.get("inProgressAppointments"));
            orderStatusData.add(inProgress);
            
            Map<String, Object> completed = new HashMap<>();
            completed.put("name", "已完成");
            completed.put("value", stats.get("completedAppointments"));
            orderStatusData.add(completed);
            
            reportData.put("orderStatusData", orderStatusData);
            
            // 服务分类分布（用于饼图）
            List<?> categoryDistribution = (List<?>) stats.get("categoryDistribution");
            reportData.put("serviceRankData", categoryDistribution);
            
            // 收入趋势数据（最近30天）
            List<Map<String, Object>> revenueTrend = adminService.getRevenueTrend(30);
            reportData.put("revenueChart", revenueTrend);
            
            // 用户增长趋势数据（最近30天）
            List<Map<String, Object>> userGrowthTrend = adminService.getUserGrowthTrend(30);
            reportData.put("userGrowthData", userGrowthTrend);
            
            return ResponseEntity.ok(ApiResponse.success("获取报表数据成功", reportData));
        } catch (Exception e) {
            System.out.println("AdminController.getReports error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取报表数据失败: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/reports/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRevenueStats(@RequestParam(required = false) String startDate,
                                           @RequestParam(required = false) String endDate) {
        try {
            System.out.println("AdminController.getRevenueStats called - 获取收入统计");
            
            Map<String, Object> revenueData = new HashMap<>();
            revenueData.put("totalRevenue", 15680.50);
            revenueData.put("monthlyGrowth", 12.5);
            revenueData.put("avgOrderValue", 98.5);
            
            return ResponseEntity.ok(ApiResponse.success("获取收入统计成功", revenueData));
        } catch (Exception e) {
            System.out.println("AdminController.getRevenueStats error: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取收入统计失败: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/reports/appointments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAppointmentStats(@RequestParam(required = false) String startDate,
                                               @RequestParam(required = false) String endDate) {
        try {
            System.out.println("AdminController.getAppointmentStats called - 获取预约统计");
            
            Map<String, Object> appointmentData = new HashMap<>();
            appointmentData.put("totalAppointments", 156);
            appointmentData.put("completedAppointments", 142);
            appointmentData.put("cancelledAppointments", 14);
            appointmentData.put("completionRate", 91.0);
            
            return ResponseEntity.ok(ApiResponse.success("获取预约统计成功", appointmentData));
        } catch (Exception e) {
            System.out.println("AdminController.getAppointmentStats error: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取预约统计失败: " + e.getMessage(), 500));
        }
    }

    /**
     * 获取所有系统配置
     */
    @GetMapping("/system/configs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSystemConfigs() {
        try {
            // 初始化默认配置（如果不存在）
            systemConfigService.initDefaultConfigs();
            
            return ResponseEntity.ok(ApiResponse.success("获取系统配置成功", 
                systemConfigService.getAllConfigs()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取系统配置失败: " + e.getMessage(), 500));
        }
    }

    /**
     * 更新系统配置
     */
    @PutMapping("/system/configs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSystemConfig(@RequestBody Map<String, String> request) {
        try {
            String configKey = request.get("configKey");
            String configValue = request.get("configValue");
            
            if (configKey == null || configKey.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("配置键不能为空", 400));
            }
            
            SystemConfig config = systemConfigService.saveOrUpdateConfig(configKey, configValue);
            
            return ResponseEntity.ok(ApiResponse.success("更新系统配置成功", config));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新系统配置失败: " + e.getMessage(), 500));
        }
    }

    /**
     * 根据配置键获取配置
     */
    @GetMapping("/system/configs/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSystemConfigByKey(@PathVariable String key) {
        try {
            return systemConfigService.getConfigByKey(key)
                    .map(config -> ResponseEntity.ok(ApiResponse.success("获取配置成功", config)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取配置失败: " + e.getMessage(), 500));
        }
    }

    // 内部类用于请求参数
    public static class UpdateStatusRequest {
        private String status;
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
    }
    
    public static class AssignEmployeeRequest {
        private Long employeeId;
        
        public Long getEmployeeId() {
            return employeeId;
        }
        
        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }
    }
}
