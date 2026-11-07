package com.autobeauty.service;

import com.autobeauty.dto.AppointmentResponse;
import com.autobeauty.entity.Appointment;
import com.autobeauty.entity.Employee;
import com.autobeauty.entity.Service;
import com.autobeauty.entity.User;
import com.autobeauty.util.ImageUtils;
import com.autobeauty.entity.Vehicle;
import com.autobeauty.repository.AppointmentRepository;
import com.autobeauty.repository.EmployeeRepository;
import com.autobeauty.repository.ReviewRepository;
import com.autobeauty.repository.ServiceRepository;
import com.autobeauty.repository.UserRepository;
import com.autobeauty.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AdminService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取管理员统计数据
     */
    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总用户数
        long totalUsers = userRepository.count();
        stats.put("totalUsers", totalUsers);
        
        // 活跃用户数（有预约的用户）
        long activeUsers = appointmentRepository.findAll().stream()
            .map(Appointment::getUserId)
            .distinct()
            .count();
        stats.put("activeUsers", activeUsers);
        
        // 今日新增用户
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayNewUsers = userRepository.findAll().stream()
            .filter(user -> user.getCreatedAt() != null && user.getCreatedAt().isAfter(todayStart))
            .count();
        stats.put("todayNewUsers", todayNewUsers);
        
        // 总积分
        int totalPoints = userRepository.findAll().stream()
            .mapToInt(user -> user.getPoints() != null ? user.getPoints() : 0)
            .sum();
        stats.put("totalPoints", totalPoints);
        
        // 总预约数
        long totalAppointments = appointmentRepository.count();
        stats.put("totalAppointments", totalAppointments);
        
        // 待确认预约数
        long pendingAppointments = appointmentRepository.findAll().stream()
            .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.PENDING)
            .count();
        stats.put("pendingAppointments", pendingAppointments);
        
        // 已确认预约数
        long confirmedAppointments = appointmentRepository.findAll().stream()
            .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.CONFIRMED)
            .count();
        stats.put("confirmedAppointments", confirmedAppointments);
        
        // 进行中预约数
        long inProgressAppointments = appointmentRepository.findAll().stream()
            .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.IN_PROGRESS)
            .count();
        stats.put("inProgressAppointments", inProgressAppointments);
        
        // 已完成预约数
        long completedAppointments = appointmentRepository.findAll().stream()
            .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.COMPLETED)
            .count();
        stats.put("completedAppointments", completedAppointments);
        
        // 今日新增预约
        long todayNewAppointments = appointmentRepository.findAll().stream()
            .filter(apt -> apt.getCreatedAt() != null && apt.getCreatedAt().isAfter(todayStart))
            .count();
        stats.put("todayNewAppointments", todayNewAppointments);
        
        // 服务项目数
        long totalServices = serviceRepository.count();
        stats.put("totalServices", totalServices);
        
        // 总评价数
        long totalReviews = reviewRepository.count();
        stats.put("totalReviews", totalReviews);
        
        // 平均评分
        List<com.autobeauty.entity.Review> allReviews = reviewRepository.findAll();
        double averageRating = 0.0;
        if (!allReviews.isEmpty()) {
            averageRating = allReviews.stream()
                .mapToDouble(review -> review.getRating() != null ? review.getRating() : 0.0)
                .average()
                .orElse(0.0);
        }
        stats.put("averageRating", Math.round(averageRating * 10.0) / 10.0);
        
        // 待回复评价数
        long pendingReviews = allReviews.stream()
            .filter(review -> review.getReply() == null || review.getReply().isEmpty())
            .count();
        stats.put("pendingReviews", pendingReviews);
        
        // 今日新增评价
        long todayNewReviews = allReviews.stream()
            .filter(review -> review.getCreatedAt() != null && review.getCreatedAt().isAfter(todayStart))
            .count();
        stats.put("todayNewReviews", todayNewReviews);
        
        // 总收入（已支付的预约）
        try {
            List<Appointment> allAppointments = appointmentRepository.findAll();
            BigDecimal totalRevenue = allAppointments.stream()
                .filter(apt -> apt.getPaymentStatus() == Appointment.PaymentStatus.PAID)
                .map(Appointment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalRevenue", totalRevenue);
        } catch (Exception e) {
            stats.put("totalRevenue", BigDecimal.ZERO);
        }
        
        // 服务分类预约分布
        try {
            List<Appointment> allAppointments = appointmentRepository.findAll();
            Map<String, Long> categoryStats = new HashMap<>();
            
            for (Appointment apt : allAppointments) {
                Optional<Service> serviceOpt = serviceRepository.findById(apt.getServiceId());
                if (serviceOpt.isPresent()) {
                    String category = serviceOpt.get().getCategory();
                    if (category != null) {
                        categoryStats.put(category, categoryStats.getOrDefault(category, 0L) + 1);
                    }
                }
            }
            
            // 转换为列表格式
            List<Map<String, Object>> categoryList = new ArrayList<>();
            for (Map.Entry<String, Long> entry : categoryStats.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", entry.getKey());
                item.put("value", entry.getValue());
                categoryList.add(item);
            }
            stats.put("categoryDistribution", categoryList);
        } catch (Exception e) {
            stats.put("categoryDistribution", new ArrayList<>());
        }
        
        return stats;
    }
    
    /**
     * 获取最近N天的预约趋势数据
     */
    public List<Map<String, Object>> getAppointmentTrend(int days) {
        List<Map<String, Object>> trendData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        System.out.println("开始统计最近" + days + "天的预约趋势");
        
        // 获取所有预约
        List<Appointment> allAppointments = appointmentRepository.findAll();
        System.out.println("数据库中总预约数: " + allAppointments.size());
        
        // 从今天往前推N天
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = now.minusDays(i).withHour(23).withMinute(59).withSecond(59);
            
            // 统计当天创建的预约数量（使用createdAt）
            long count = allAppointments.stream()
                .filter(apt -> apt.getCreatedAt() != null && 
                              !apt.getCreatedAt().isBefore(dayStart) && 
                              apt.getCreatedAt().isBefore(dayEnd))
                .count();
            
            System.out.println(dayStart.toLocalDate() + " 的预约数: " + count);
            
            Map<String, Object> dayData = new HashMap<>();
            // 格式化日期显示（周一、周二等）
            String[] weekDays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
            int dayOfWeek = dayStart.getDayOfWeek().getValue() % 7; // 0=周日, 1=周一...6=周六
            dayData.put("date", weekDays[dayOfWeek]);
            dayData.put("count", count);
            dayData.put("fullDate", dayStart.toLocalDate().toString());
            
            trendData.add(dayData);
        }
        
        System.out.println("返回趋势数据: " + trendData.size() + " 天");
        return trendData;
    }
    
    /**
     * 获取最近N天的收入趋势数据
     */
    public List<Map<String, Object>> getRevenueTrend(int days) {
        List<Map<String, Object>> trendData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // 获取所有已支付的预约
        List<Appointment> allAppointments = appointmentRepository.findAll();
        
        // 从今天往前推N天
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = now.minusDays(i).withHour(23).withMinute(59).withSecond(59);
            
            // 统计当天的收入（已支付的预约）
            BigDecimal dayRevenue = allAppointments.stream()
                .filter(apt -> apt.getPaymentStatus() == Appointment.PaymentStatus.PAID &&
                              apt.getPaymentTime() != null &&
                              !apt.getPaymentTime().isBefore(dayStart) && 
                              apt.getPaymentTime().isBefore(dayEnd))
                .map(Appointment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dayStart.toLocalDate().toString());
            dayData.put("revenue", dayRevenue);
            
            trendData.add(dayData);
        }
        
        return trendData;
    }
    
    /**
     * 获取最近N天的用户增长趋势数据
     */
    public List<Map<String, Object>> getUserGrowthTrend(int days) {
        List<Map<String, Object>> trendData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // 获取所有用户
        List<User> allUsers = userRepository.findAll();
        // 获取所有预约
        List<Appointment> allAppointments = appointmentRepository.findAll();
        
        // 从今天往前推N天
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = now.minusDays(i).withHour(23).withMinute(59).withSecond(59);
            
            // 统计当天新增用户数
            long newUsers = allUsers.stream()
                .filter(user -> user.getCreatedAt() != null &&
                               !user.getCreatedAt().isBefore(dayStart) && 
                               user.getCreatedAt().isBefore(dayEnd))
                .count();
            
            // 统计当天活跃用户数（有预约的用户）
            List<Long> activeUserIds = allAppointments.stream()
                .filter(apt -> apt.getCreatedAt() != null &&
                              !apt.getCreatedAt().isBefore(dayStart) && 
                              apt.getCreatedAt().isBefore(dayEnd))
                .map(Appointment::getUserId)
                .distinct()
                .collect(Collectors.toList());
            
            long activeUsers = activeUserIds.size();
            
            // 统计当天的订单数（预约数）
            long totalOrders = allAppointments.stream()
                .filter(apt -> apt.getCreatedAt() != null &&
                              !apt.getCreatedAt().isBefore(dayStart) && 
                              apt.getCreatedAt().isBefore(dayEnd))
                .count();
            
            // 统计当天的消费总额（已支付的预约）
            BigDecimal totalSpending = allAppointments.stream()
                .filter(apt -> apt.getPaymentStatus() == Appointment.PaymentStatus.PAID &&
                              apt.getPaymentTime() != null &&
                              !apt.getPaymentTime().isBefore(dayStart) && 
                              apt.getPaymentTime().isBefore(dayEnd))
                .map(Appointment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 计算人均订单数和人均消费
            double averageOrders = activeUsers > 0 ? (double) totalOrders / activeUsers : 0;
            BigDecimal averageSpending = activeUsers > 0 
                ? totalSpending.divide(BigDecimal.valueOf(activeUsers), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dayStart.toLocalDate().toString());
            dayData.put("newUsers", newUsers);
            dayData.put("activeUsers", activeUsers);
            dayData.put("totalOrders", totalOrders);
            dayData.put("totalSpending", totalSpending);
            dayData.put("averageOrders", Math.round(averageOrders * 10.0) / 10.0); // 保留1位小数
            dayData.put("averageSpending", averageSpending);
            
            trendData.add(dayData);
        }
        
        return trendData;
    }

    /**
     * 获取最近预约
     */
    public Page<AppointmentResponse> getRecentAppointments(Pageable pageable) {
        Page<Appointment> appointments = appointmentRepository.findAllByOrderByCreatedAtDesc(pageable);
        
        List<AppointmentResponse> responses = appointments.getContent().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
            
        return new PageImpl<>(responses, pageable, appointments.getTotalElements());
    }

    /**
     * 获取所有预约（管理员）
     */
    public Page<AppointmentResponse> getAllAppointments(Pageable pageable, String status) {
        Page<Appointment> appointments;
        
        if (status != null && !status.isEmpty()) {
            try {
                // 将字符串转换为枚举类型
                Appointment.AppointmentStatus statusEnum = Appointment.AppointmentStatus.valueOf(status);
                appointments = appointmentRepository.findByStatusOrderByCreatedAtDesc(statusEnum, pageable);
            } catch (IllegalArgumentException e) {
                // 如果转换失败，返回所有预约
                appointments = appointmentRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
        } else {
            appointments = appointmentRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        
        List<AppointmentResponse> responses = appointments.getContent().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
            
        return new PageImpl<>(responses, pageable, appointments.getTotalElements());
    }
    
    /**
     * 搜索预约（支持多条件）
     */
    public Page<AppointmentResponse> searchAppointments(String orderNo, String username, Long serviceId, 
                                                        String status, String paymentStatus, Pageable pageable) {
        // 获取所有预约
        List<Appointment> allAppointments = appointmentRepository.findAll();
        
        // 应用筛选条件
        List<Appointment> filteredAppointments = allAppointments.stream()
            .filter(appointment -> {
                // 按订单号筛选
                if (orderNo != null && !orderNo.trim().isEmpty()) {
                    if (!appointment.getOrderNo().contains(orderNo)) {
                        return false;
                    }
                }
                
                // 按用户名筛选
                if (username != null && !username.trim().isEmpty()) {
                    Optional<User> userOpt = userRepository.findById(appointment.getUserId());
                    if (!userOpt.isPresent() || !userOpt.get().getUsername().contains(username)) {
                        return false;
                    }
                }
                
                // 按服务ID筛选
                if (serviceId != null && !appointment.getServiceId().equals(serviceId)) {
                    return false;
                }
                
                // 按预约状态筛选
                if (status != null && !status.trim().isEmpty()) {
                    try {
                        Appointment.AppointmentStatus statusEnum = Appointment.AppointmentStatus.valueOf(status);
                        if (!appointment.getStatus().equals(statusEnum)) {
                            return false;
                        }
                    } catch (IllegalArgumentException e) {
                        // 忽略无效的状态值
                    }
                }
                
                // 按支付状态筛选
                if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
                    try {
                        Appointment.PaymentStatus paymentStatusEnum = Appointment.PaymentStatus.valueOf(paymentStatus);
                        if (!appointment.getPaymentStatus().equals(paymentStatusEnum)) {
                            return false;
                        }
                    } catch (IllegalArgumentException e) {
                        // 忽略无效的支付状态值
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredAppointments.size());
        List<Appointment> pageContent = filteredAppointments.subList(start, end);
        
        // 转换为AppointmentResponse
        List<AppointmentResponse> responses = pageContent.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(responses, pageable, filteredAppointments.size());
    }

    /**
     * 根据日期范围获取预约
     */
    public Page<AppointmentResponse> getAppointmentsByDateRange(String startDate, String endDate, Pageable pageable) {
        try {
            // 解析日期字符串
            LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
            
            // 查询指定日期范围内的预约
            List<Appointment> appointments = appointmentRepository.findByAppointmentTimeBetween(startDateTime, endDateTime);
            
            // 手动分页
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), appointments.size());
            List<Appointment> pageContent = appointments.subList(start, end);
            
            List<AppointmentResponse> responses = pageContent.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
                
            return new PageImpl<>(responses, pageable, appointments.size());
        } catch (Exception e) {
            // 如果日期解析失败，返回空结果
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    /**
     * 更新预约状态
     */
    public void updateAppointmentStatus(Long appointmentId, String status) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (!appointmentOpt.isPresent()) {
            throw new RuntimeException("预约不存在");
        }
        
        Appointment appointment = appointmentOpt.get();
        Appointment.AppointmentStatus oldStatus = appointment.getStatus();
        Appointment.AppointmentStatus newStatus = Appointment.AppointmentStatus.valueOf(status);
        
        appointment.setStatus(newStatus);
        appointmentRepository.save(appointment);
        
        // 如果状态从非COMPLETED变为COMPLETED，给用户增加积分
        if (newStatus == Appointment.AppointmentStatus.COMPLETED && 
            oldStatus != Appointment.AppointmentStatus.COMPLETED &&
            appointment.getPaymentStatus() == Appointment.PaymentStatus.PAID) {
            int points = appointment.getTotalAmount().intValue();
            String reason = "完成预约订单获得积分";
            userService.updateUserPoints(appointment.getUserId(), points, reason, appointmentId);
        }
    }

    /**
     * 分配员工
     */
    public void assignEmployee(Long appointmentId, Long employeeId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (!appointmentOpt.isPresent()) {
            throw new RuntimeException("预约不存在");
        }
        
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        if (!employeeOpt.isPresent()) {
            throw new RuntimeException("员工不存在");
        }
        
        Appointment appointment = appointmentOpt.get();
        appointment.setEmployeeId(employeeId);
        appointmentRepository.save(appointment);
    }

    /**
     * 获取所有用户（带统计信息）
     */
    public Page<Map<String, Object>> getAllUsers(Pageable pageable) {
        Page<User> usersPage = userRepository.findAll(pageable);
        return convertUsersToResponse(usersPage, pageable);
    }
    
    /**
     * 搜索用户（支持多条件）
     */
    public Page<Map<String, Object>> searchUsers(String username, String realName, String phone, String status, Pageable pageable) {
        // 获取所有用户并在内存中过滤
        List<User> allUsers = userRepository.findAll();
        
        // 应用筛选条件
        List<User> filteredUsers = allUsers.stream()
            .filter(user -> {
                if (username != null && !username.trim().isEmpty()) {
                    if (!user.getUsername().contains(username)) {
                        return false;
                    }
                }
                if (realName != null && !realName.trim().isEmpty()) {
                    if (user.getRealName() == null || !user.getRealName().contains(realName)) {
                        return false;
                    }
                }
                if (phone != null && !phone.trim().isEmpty()) {
                    if (user.getPhone() == null || !user.getPhone().contains(phone)) {
                        return false;
                    }
                }
                if (status != null && !status.trim().isEmpty()) {
                    String userStatus = user.getStatus() == 1 ? "ACTIVE" : "DISABLED";
                    if (!userStatus.equals(status)) {
                        return false;
                    }
                }
                return true;
            })
            .collect(Collectors.toList());
        
        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredUsers.size());
        List<User> pageContent = filteredUsers.subList(start, end);
        
        Page<User> usersPage = new PageImpl<>(pageContent, pageable, filteredUsers.size());
        return convertUsersToResponse(usersPage, pageable);
    }
    
    /**
     * 将用户列表转换为响应格式（包含统计信息）
     */
    private Page<Map<String, Object>> convertUsersToResponse(Page<User> usersPage, Pageable pageable) {
        List<Map<String, Object>> usersList = usersPage.getContent().stream()
            .map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("email", user.getEmail());
                // 用户联系电话：普通用户来自 users 表；员工账号来自 employees 表
                String phone = user.getPhone();
                if (user.getRole() == User.Role.EMPLOYEE && user.getEmployeeId() != null) {
                    Optional<Employee> employeeOpt = employeeRepository.findById(user.getEmployeeId());
                    if (employeeOpt.isPresent() && employeeOpt.get().getPhone() != null) {
                        phone = employeeOpt.get().getPhone();
                    }
                }
                userMap.put("phone", phone);
                userMap.put("realName", user.getRealName());
                userMap.put("gender", user.getGender());
                // 用户头像：普通用户来自 users 表；员工头像来自 employees 表
                String avatarUrl = user.getAvatarUrl();
                if (user.getRole() == User.Role.EMPLOYEE && user.getEmployeeId() != null) {
                    Optional<Employee> employeeOpt = employeeRepository.findById(user.getEmployeeId());
                    if (employeeOpt.isPresent() && employeeOpt.get().getAvatarUrl() != null) {
                        avatarUrl = employeeOpt.get().getAvatarUrl();
                    }
                }
                userMap.put("avatarUrl", avatarUrl);
                userMap.put("address", user.getAddress());
                userMap.put("points", user.getPoints());
                userMap.put("status", user.getStatus() == 1 ? "ACTIVE" : "DISABLED");
                userMap.put("role", user.getRole());
                // 当用户角色为 EMPLOYEE 时，前端详情页需要 employeeId 去拉取 employees 表详情
                userMap.put("employeeId", user.getEmployeeId());
                userMap.put("createdAt", user.getCreatedAt());
                userMap.put("updatedAt", user.getUpdatedAt());
                
                // 统计车辆数
                long vehicleCount = vehicleRepository.countByUserId(user.getId());
                userMap.put("vehicleCount", vehicleCount);
                
                // 统计预约数
                long appointmentCount = appointmentRepository.countByUserId(user.getId());
                userMap.put("appointmentCount", appointmentCount);
                
                return userMap;
            })
            .collect(Collectors.toList());
        
        return new PageImpl<>(usersList, pageable, usersPage.getTotalElements());
    }
    
    /**
     * 更新用户信息（管理员）
     */
    public void updateUser(Long userId, Map<String, Object> userData) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        // 更新允许修改的字段
        if (userData.containsKey("realName")) {
            user.setRealName((String) userData.get("realName"));
        }
        if (userData.containsKey("email")) {
            user.setEmail((String) userData.get("email"));
        }
        if (userData.containsKey("phone")) {
            user.setPhone((String) userData.get("phone"));
        }
        if (userData.containsKey("address")) {
            user.setAddress((String) userData.get("address"));
        }
        if (userData.containsKey("gender")) {
            user.setGender((Integer) userData.get("gender"));
        }
        if (userData.containsKey("status")) {
            String status = (String) userData.get("status");
            user.setStatus(status.equals("ACTIVE") ? 1 : 0);
        }
        
        userRepository.save(user);
    }
    
    /**
     * 更新用户积分（管理员）
     */
    public void updateUserPoints(Long userId, String type, Integer amount, String reason) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        switch (type) {
            case "ADD":
                // 增加积分
                userService.updateUserPoints(userId, amount, "管理员增加积分：" + reason, null);
                break;
            case "SUBTRACT":
                // 扣除积分
                if (user.getPoints() < amount) {
                    throw new RuntimeException("用户积分不足，当前积分：" + user.getPoints());
                }
                userService.updateUserPoints(userId, -amount, "管理员扣除积分：" + reason, null);
                break;
            case "SET":
                // 设置积分
                int currentPoints = user.getPoints();
                int pointsChange = amount - currentPoints;
                
                if (pointsChange != 0) {
                    userService.updateUserPoints(userId, pointsChange, "管理员设置积分：" + reason, null);
                }
                break;
            default:
                throw new RuntimeException("无效的操作类型");
        }
    }
    
    /**
     * 更新用户状态（管理员）
     */
    public void updateUserStatus(Long userId, String status) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        user.setStatus(status.equals("ACTIVE") ? 1 : 0);
        userRepository.save(user);
    }
    
    /**
     * 重置用户密码（管理员）
     */
    public String resetUserPassword(Long userId, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        // 如果没有提供新密码，生成一个随机密码
        if (newPassword == null || newPassword.trim().isEmpty()) {
            newPassword = generateRandomPassword();
        }
        
        // 验证密码长度
        if (newPassword.length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }
        
        // 加密并设置新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return newPassword;
    }
    
    /**
     * 生成随机密码
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz23456789";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    /**
     * 获取用户详情（带完整统计信息）
     */
    public Map<String, Object> getUserById(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        Map<String, Object> userDetail = new HashMap<>();
        // 基本信息
        userDetail.put("id", user.getId());
        userDetail.put("username", user.getUsername());
        userDetail.put("email", user.getEmail());
        userDetail.put("phone", user.getPhone());
        userDetail.put("realName", user.getRealName());
        userDetail.put("gender", user.getGender());
        // 用户头像：普通用户来自 users 表；员工头像来自 employees 表
        String avatarUrl = user.getAvatarUrl();
        if (user.getRole() == User.Role.EMPLOYEE && user.getEmployeeId() != null) {
            Optional<Employee> employeeOpt = employeeRepository.findById(user.getEmployeeId());
            if (employeeOpt.isPresent() && employeeOpt.get().getAvatarUrl() != null) {
                avatarUrl = employeeOpt.get().getAvatarUrl();
            }
        }
        userDetail.put("avatarUrl", avatarUrl);
        userDetail.put("address", user.getAddress());
        userDetail.put("points", user.getPoints());
        userDetail.put("status", user.getStatus() == 1 ? "ACTIVE" : "DISABLED");
        userDetail.put("role", user.getRole());
        // 当 role=EMPLOYEE 时，关联到 employees 表
        userDetail.put("employeeId", user.getEmployeeId());
        userDetail.put("createdAt", user.getCreatedAt());
        userDetail.put("updatedAt", user.getUpdatedAt());
        
        // 统计车辆数
        long vehicleCount = vehicleRepository.countByUserId(userId);
        userDetail.put("vehicleCount", vehicleCount);
        
        // 统计预约数
        long appointmentCount = appointmentRepository.countByUserId(userId);
        userDetail.put("appointmentCount", appointmentCount);
        
        // 统计评价数
        long reviewCount = reviewRepository.countByUserId(userId);
        System.out.println("用户ID " + userId + " 的评价数量: " + reviewCount);
        userDetail.put("reviewCount", reviewCount);
        
        // 统计消费金额（所有已支付的预约）
        List<Appointment> paidAppointments = appointmentRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged()).getContent();
        System.out.println("用户ID " + userId + " 的预约总数: " + paidAppointments.size());
        
        BigDecimal totalAmount = paidAppointments.stream()
            .filter(apt -> {
                boolean isPaid = apt.getPaymentStatus() == Appointment.PaymentStatus.PAID;
                System.out.println("预约ID " + apt.getId() + " 支付状态: " + apt.getPaymentStatus() + ", 是否已支付: " + isPaid + ", 金额: " + apt.getTotalAmount());
                return isPaid;
            })
            .map(Appointment::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        System.out.println("用户ID " + userId + " 的消费总额: " + totalAmount);
        userDetail.put("totalAmount", totalAmount);
        
        return userDetail;
    }

    /**
     * 获取所有员工
     */
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }
    
    /**
     * 搜索员工（支持多条件）
     */
    public Page<Employee> searchEmployees(String name, String phone, String skills, Pageable pageable) {
        // 获取所有员工
        List<Employee> allEmployees = employeeRepository.findAll();
        
        // 应用筛选条件
        List<Employee> filteredEmployees = allEmployees.stream()
            .filter(employee -> {
                // 按姓名筛选
                if (name != null && !name.trim().isEmpty()) {
                    if (!employee.getName().contains(name)) {
                        return false;
                    }
                }
                
                // 按电话筛选
                if (phone != null && !phone.trim().isEmpty()) {
                    if (employee.getPhone() == null || !employee.getPhone().contains(phone)) {
                        return false;
                    }
                }
                
                // 按技能筛选
                if (skills != null && !skills.trim().isEmpty()) {
                    if (employee.getSkills() == null || !employee.getSkills().contains(skills)) {
                        return false;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredEmployees.size());
        List<Employee> pageContent = filteredEmployees.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, filteredEmployees.size());
    }

    /**
     * 获取所有服务（管理员，带分页）
     */
    public Page<Service> getAllServicesForAdmin(Pageable pageable) {
        // 删除是软删除（status=0），列表默认只展示启用的服务，避免“重置筛选后又看见已删除项”
        return serviceRepository.findByStatus(1, pageable);
    }
    
    /**
     * 搜索服务（支持多条件）
     */
    public Page<Service> searchServices(String name, String category, String status, Pageable pageable) {
        // 获取所有服务
        List<Service> allServices = serviceRepository.findAll();
        
        // 删除是软删除（status=0），如果前端没有传状态/传空，则默认只展示启用的服务（status=1）
        // 注意：lambda 内引用的变量必须是 final/实际上最终（effective final）
        final Integer effectiveStatus = parseEffectiveStatus(status);

        // 应用筛选条件
        List<Service> filteredServices = allServices.stream()
            .filter(service -> {
                // 按服务名称筛选
                if (name != null && !name.trim().isEmpty()) {
                    if (!service.getName().contains(name)) {
                        return false;
                    }
                }
                
                // 按分类筛选（支持中英文匹配）
                if (category != null && !category.trim().isEmpty()) {
                    if (service.getCategory() == null) {
                        return false;
                    }
                    // 精确匹配或包含匹配
                    if (!service.getCategory().equals(category) && 
                        !service.getCategory().contains(category)) {
                        return false;
                    }
                }
                
                // 按状态筛选（删除是软删除，默认 effectiveStatus=1）
                if (service.getStatus() == null || !service.getStatus().equals(effectiveStatus)) {
                    return false;
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredServices.size());
        List<Service> pageContent = filteredServices.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, filteredServices.size());
    }

    /**
     * 解析前端传入的状态字符串为有效的筛选状态。
     * 如果前端不传状态（null/空），默认返回 1（启用）。
     */
    private Integer parseEffectiveStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return 1;
        }
        try {
            return Integer.parseInt(status);
        } catch (NumberFormatException e) {
            return "ACTIVE".equals(status) ? 1 : 0;
        }
    }

    /**
     * 创建员工
     */
    public Employee createEmployee(Employee employee) {
        // 设置默认状态为在职
        if (employee.getStatus() == null) {
            employee.setStatus(1);
        }
        
        // 处理Base64图片数据
        if (employee.getAvatarUrl() != null && ImageUtils.isDataUrl(employee.getAvatarUrl())) {
            byte[] imageData = ImageUtils.decodeDataUrl(employee.getAvatarUrl());
            if (imageData != null) {
                employee.setAvatarData(imageData);
                // 暂时保留avatarUrl，保存后会更新为数据库访问URL
            }
        }
        
        // 保持avatarUrl为Base64格式，前端可以直接显示
        return employeeRepository.save(employee);
    }

    /**
     * 更新员工
     */
    public Employee updateEmployee(Long id, Employee employee) {
        Optional<Employee> existingEmployeeOpt = employeeRepository.findById(id);
        if (!existingEmployeeOpt.isPresent()) {
            throw new RuntimeException("员工不存在");
        }
        
        Employee existingEmployee = existingEmployeeOpt.get();
        
        // 只更新非空字段
        if (employee.getName() != null && !employee.getName().trim().isEmpty()) {
            existingEmployee.setName(employee.getName());
        }
        if (employee.getPhone() != null) {
            existingEmployee.setPhone(employee.getPhone());
        }
        if (employee.getSkills() != null) {
            existingEmployee.setSkills(employee.getSkills());
        }
        if (employee.getWorkSchedule() != null) {
            existingEmployee.setWorkSchedule(employee.getWorkSchedule());
        }
        if (employee.getAvatarUrl() != null) {
            // 处理Base64图片数据
            if (ImageUtils.isDataUrl(employee.getAvatarUrl())) {
                byte[] imageData = ImageUtils.decodeDataUrl(employee.getAvatarUrl());
                if (imageData != null) {
                    existingEmployee.setAvatarData(imageData);
                    existingEmployee.setAvatarUrl(employee.getAvatarUrl()); // 保持Base64格式
                }
            } else {
                existingEmployee.setAvatarUrl(employee.getAvatarUrl());
            }
        }
        if (employee.getStatus() != null) {
            existingEmployee.setStatus(employee.getStatus());
        }
        
        return employeeRepository.save(existingEmployee);
    }

    /**
     * 删除员工
     */
    public void deleteEmployee(Long id) {
        // 允许幂等：employees 记录可能已删，但 users 里 employee_id 对应账号还在。
        Optional<Employee> employeeOpt = employeeRepository.findById(id);

        Optional<User> userOpt = userRepository.findByEmployeeId(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 只删除“员工账号”（role=EMPLOYEE），避免误删其他业务账号
            if (user.getRole() == User.Role.EMPLOYEE) {
                userRepository.delete(user);
            }
        }

        // 如果 employees 还存在，再删除 employees 记录
        if (employeeOpt.isPresent()) {
            employeeRepository.delete(employeeOpt.get());
            return;
        }

        // employees 不存在，但 users 也不存在，才算真正“员工不存在”
        if (!userOpt.isPresent()) {
            throw new RuntimeException("员工不存在");
        }
    }

    /**
     * 根据ID获取员工
     */
    public Employee getEmployeeById(Long id) {
        Optional<Employee> employeeOpt = employeeRepository.findById(id);
        if (!employeeOpt.isPresent()) {
            throw new RuntimeException("员工不存在");
        }
        return employeeOpt.get();
    }
    
    /**
     * 转换预约实体为响应DTO
     */
    private AppointmentResponse convertToResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setOrderNo(appointment.getOrderNo());
        response.setUserId(appointment.getUserId());
        response.setServiceId(appointment.getServiceId());
        response.setVehicleId(appointment.getVehicleId());
        response.setEmployeeId(appointment.getEmployeeId());
        response.setAppointmentTime(appointment.getAppointmentTime());
        response.setStatus(appointment.getStatus());
        response.setNotes(appointment.getNotes());
        response.setTotalAmount(appointment.getTotalAmount());
        response.setPaymentStatus(appointment.getPaymentStatus());
        response.setPaymentTime(appointment.getPaymentTime());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());
        
        // 加载关联的用户信息
        if (appointment.getUserId() != null) {
            Optional<User> userOpt = userRepository.findById(appointment.getUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                response.setUser(new AppointmentResponse.UserInfo(
                    user.getId(), user.getUsername(), user.getRealName(), 
                    user.getPhone(), user.getEmail()));
            }
        }
        
        // 加载关联的车辆信息
        if (appointment.getVehicleId() != null) {
            Optional<Vehicle> vehicleOpt = vehicleRepository.findById(appointment.getVehicleId());
            if (vehicleOpt.isPresent()) {
                Vehicle vehicle = vehicleOpt.get();
                response.setVehicle(new AppointmentResponse.VehicleInfo(
                    vehicle.getId(), vehicle.getBrand(), vehicle.getModel(), 
                    vehicle.getLicensePlate(), vehicle.getColor()));
            }
        }
        
        // 加载关联的服务信息
        if (appointment.getServiceId() != null) {
            Optional<Service> serviceOpt = serviceRepository.findById(appointment.getServiceId());
            if (serviceOpt.isPresent()) {
                Service service = serviceOpt.get();
                response.setService(new AppointmentResponse.ServiceInfo(
                    service.getId(), service.getName(), service.getDescription(),
                    service.getPrice(), service.getDuration(), service.getCategory()));
            }
        }
        
        // 加载关联的员工信息
        if (appointment.getEmployeeId() != null) {
            Optional<Employee> employeeOpt = employeeRepository.findById(appointment.getEmployeeId());
            if (employeeOpt.isPresent()) {
                Employee employee = employeeOpt.get();
                response.setEmployee(new AppointmentResponse.EmployeeInfo(
                    employee.getId(), employee.getName(), employee.getPhone(), employee.getSkills()));
            }
        }
        
        return response;
    }
}
