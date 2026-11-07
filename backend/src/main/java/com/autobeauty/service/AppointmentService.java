package com.autobeauty.service;

import com.autobeauty.dto.AppointmentRequest;
import com.autobeauty.dto.AppointmentResponse;
import com.autobeauty.entity.Appointment;
import com.autobeauty.entity.Employee;
import com.autobeauty.entity.Service;
import com.autobeauty.entity.Vehicle;
import com.autobeauty.repository.AppointmentRepository;
import com.autobeauty.repository.EmployeeRepository;
import com.autobeauty.repository.ServiceRepository;
import com.autobeauty.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@org.springframework.stereotype.Service
@Transactional
public class AppointmentService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private UserService userService;
    
    /**
     * 创建预约
     */
    public Appointment createAppointment(AppointmentRequest request, Long userId) {
        // 验证车辆是否属于用户
        Optional<Vehicle> vehicleOpt = vehicleRepository.findByIdAndUserId(request.getVehicleId(), userId);
        if (!vehicleOpt.isPresent()) {
            throw new RuntimeException("车辆不存在或无权限操作");
        }
        
        // 验证服务是否存在且启用
        Optional<Service> serviceOpt = serviceRepository.findByIdAndStatus(request.getServiceId(), 1);
        if (!serviceOpt.isPresent()) {
            throw new RuntimeException("服务项目不存在或已下架");
        }
        
        Service service = serviceOpt.get();
        
        // 检查时间冲突
        LocalDateTime endTime = request.getAppointmentTime().plusMinutes(service.getDuration());
        List<Appointment> conflictingAppointments = appointmentRepository.findConflictingAppointments(
                request.getAppointmentTime(), endTime);
        
        if (!conflictingAppointments.isEmpty()) {
            throw new RuntimeException("该时间段已被预约，请选择其他时间");
        }
        
        // 生成订单号
        String orderNo = generateOrderNo();
        
        // 创建预约
        Appointment appointment = new Appointment();
        appointment.setOrderNo(orderNo);
        appointment.setUserId(userId);
        appointment.setVehicleId(request.getVehicleId());
        appointment.setServiceId(request.getServiceId());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setTotalAmount(service.getPrice());
        appointment.setStatus(Appointment.AppointmentStatus.PENDING);
        appointment.setPaymentStatus(Appointment.PaymentStatus.UNPAID);
        appointment.setNotes(request.getNotes());
        
        return appointmentRepository.save(appointment);
    }
    
    /**
     * 根据用户ID获取预约列表
     */
    public Page<AppointmentResponse> getAppointmentsByUserId(Long userId, Pageable pageable) {
        Page<Appointment> appointments = appointmentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return appointments.map(this::convertToResponse);
    }
    
    /**
     * 根据用户ID和状态获取预约列表
     */
    public Page<AppointmentResponse> getAppointmentsByUserIdAndStatus(Long userId, Appointment.AppointmentStatus status, Pageable pageable) {
        Page<Appointment> appointments = appointmentRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        return appointments.map(this::convertToResponse);
    }
    
    /**
     * 转换为响应DTO
     */
    private AppointmentResponse convertToResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        
        // 基本信息
        response.setId(appointment.getId());
        response.setOrderNo(appointment.getOrderNo());
        response.setUserId(appointment.getUserId());
        response.setVehicleId(appointment.getVehicleId());
        response.setServiceId(appointment.getServiceId());
        response.setEmployeeId(appointment.getEmployeeId());
        response.setAppointmentTime(appointment.getAppointmentTime());
        response.setStatus(appointment.getStatus());
        response.setTotalAmount(appointment.getTotalAmount());
        response.setPaymentStatus(appointment.getPaymentStatus());
        response.setPaymentMethod(appointment.getPaymentMethod());
        response.setPaymentTime(appointment.getPaymentTime());
        response.setNotes(appointment.getNotes());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());
        
        // 加载关联数据
        if (appointment.getVehicleId() != null) {
            Optional<Vehicle> vehicleOpt = vehicleRepository.findById(appointment.getVehicleId());
            if (vehicleOpt.isPresent()) {
                Vehicle vehicle = vehicleOpt.get();
                response.setVehicle(new AppointmentResponse.VehicleInfo(
                    vehicle.getId(), vehicle.getBrand(), vehicle.getModel(), 
                    vehicle.getLicensePlate(), vehicle.getColor()));
            }
        }
        
        if (appointment.getServiceId() != null) {
            Optional<Service> serviceOpt = serviceRepository.findById(appointment.getServiceId());
            if (serviceOpt.isPresent()) {
                Service service = serviceOpt.get();
                response.setService(new AppointmentResponse.ServiceInfo(
                    service.getId(), service.getName(), service.getDescription(),
                    service.getPrice(), service.getDuration(), service.getCategory()));
            }
        }
        
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
    
    /**
     * 根据ID和用户ID获取预约详情
     */
    public Optional<Appointment> getAppointmentByIdAndUserId(Long id, Long userId) {
        return appointmentRepository.findByIdAndUserId(id, userId);
    }
    
    /**
     * 根据订单号获取预约
     */
    public Optional<Appointment> getAppointmentByOrderNo(String orderNo) {
        return appointmentRepository.findByOrderNo(orderNo);
    }
    
    /**
     * 根据ID获取预约（不检查用户ID）
     */
    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }
    
    /**
     * 更新预约状态
     */
    public Appointment updateAppointmentStatus(Long appointmentId, Appointment.AppointmentStatus status) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (!appointmentOpt.isPresent()) {
            throw new RuntimeException("预约不存在");
        }
        
        Appointment appointment = appointmentOpt.get();
        Appointment.AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(status);
        
        // 如果状态从非COMPLETED变为COMPLETED，给用户增加积分
        if (status == Appointment.AppointmentStatus.COMPLETED && 
            oldStatus != Appointment.AppointmentStatus.COMPLETED &&
            appointment.getPaymentStatus() == Appointment.PaymentStatus.PAID) {
            int points = appointment.getTotalAmount().intValue();
            String reason = "完成预约订单获得积分";
            userService.updateUserPoints(appointment.getUserId(), points, reason, appointmentId);
        }
        
        return appointmentRepository.save(appointment);
    }
    
    /**
     * 更新支付状态
     */
    public Appointment updatePaymentStatus(Long appointmentId, Appointment.PaymentStatus paymentStatus) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (!appointmentOpt.isPresent()) {
            throw new RuntimeException("预约不存在");
        }
        
        Appointment appointment = appointmentOpt.get();
        appointment.setPaymentStatus(paymentStatus);
        
        if (paymentStatus == Appointment.PaymentStatus.PAID) {
            appointment.setPaymentTime(LocalDateTime.now());
        }
        
        return appointmentRepository.save(appointment);
    }
    
    /**
     * 取消预约
     */
    public void cancelAppointment(Long appointmentId, Long userId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findByIdAndUserId(appointmentId, userId);
        if (!appointmentOpt.isPresent()) {
            throw new RuntimeException("预约不存在或无权限操作");
        }
        
        Appointment appointment = appointmentOpt.get();
        
        // 检查是否可以取消
        if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
            throw new RuntimeException("已完成的预约无法取消");
        }
        
        if (appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
            throw new RuntimeException("预约已取消");
        }
        
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }
    
    /**
     * 删除预约
     */
    public void deleteAppointment(Long appointmentId, Long userId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findByIdAndUserId(appointmentId, userId);
        if (!appointmentOpt.isPresent()) {
            throw new RuntimeException("预约不存在或无权限操作");
        }
        
        Appointment appointment = appointmentOpt.get();
        
        // 只允许删除已取消或已完成的预约
        if (appointment.getStatus() != Appointment.AppointmentStatus.CANCELLED && 
            appointment.getStatus() != Appointment.AppointmentStatus.COMPLETED) {
            throw new RuntimeException("只能删除已取消或已完成的预约");
        }
        
        appointmentRepository.delete(appointment);
    }
    
    /**
     * 获取今日预约
     */
    public List<Appointment> getTodayAppointments() {
        return appointmentRepository.findTodayAppointments();
    }
    
    /**
     * 获取即将到期的预约
     */
    public List<Appointment> getUpcomingAppointments(LocalDateTime startTime, LocalDateTime endTime) {
        return appointmentRepository.findUpcomingAppointments(startTime, endTime);
    }
    
    /**
     * 统计收入
     */
    public BigDecimal calculateRevenue(LocalDateTime startTime, LocalDateTime endTime) {
        BigDecimal revenue = appointmentRepository.calculateRevenue(startTime, endTime);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
    
    /**
     * 根据状态获取预约列表（管理员功能）
     */
    public Page<Appointment> getAppointmentsByStatus(Appointment.AppointmentStatus status, Pageable pageable) {
        return appointmentRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }
    
    /**
     * 获取可用时间段
     */
    public List<Map<String, Object>> getAvailableTimeSlots(String dateStr, Long serviceId) {
        LocalDate date = LocalDate.parse(dateStr);
        
        // 获取服务信息
        Optional<Service> serviceOpt = serviceRepository.findById(serviceId);
        if (!serviceOpt.isPresent()) {
            throw new RuntimeException("服务不存在");
        }
        
        Service service = serviceOpt.get();
        int duration = service.getDuration();
        
        // 生成时间段（9:00-18:00，每30分钟一个时间段）
        List<Map<String, Object>> timeSlots = new ArrayList<>();
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(18, 0);
        
        while (startTime.isBefore(endTime)) {
            LocalDateTime slotDateTime = LocalDateTime.of(date, startTime);
            LocalDateTime slotEndTime = slotDateTime.plusMinutes(duration);
            
            // 检查是否有冲突的预约
            List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                slotDateTime, slotEndTime);
            
            Map<String, Object> slot = new HashMap<>();
            slot.put("time", startTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            slot.put("available", conflicts.isEmpty() && slotDateTime.isAfter(LocalDateTime.now()));
            
            timeSlots.add(slot);
            
            // 下一个时间段（30分钟间隔）
            startTime = startTime.plusMinutes(30);
        }
        
        return timeSlots;
    }
    
    /**
     * 更新预约
     */
    public Appointment updateAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }
    
    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int) (Math.random() * 1000));
        return "AB" + timestamp + String.format("%03d", Integer.parseInt(random));
    }
}
