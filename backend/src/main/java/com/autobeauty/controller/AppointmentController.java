package com.autobeauty.controller;

import com.autobeauty.dto.ApiResponse;
import com.autobeauty.dto.AppointmentRequest;
import com.autobeauty.entity.Appointment;
import com.autobeauty.entity.User;
import com.autobeauty.repository.UserRepository;
import com.autobeauty.service.AppointmentService;
import com.autobeauty.service.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createAppointment(@Valid @RequestBody AppointmentRequest request, 
                                             Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Appointment appointment = appointmentService.createAppointment(request, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success("预约成功", appointment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建预约失败", 500));
        }
    }
    
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserAppointments(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(required = false) String status,
                                               Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Pageable pageable = PageRequest.of(page, size);
            Page<com.autobeauty.dto.AppointmentResponse> appointments;
            
            if (status != null && !status.isEmpty()) {
                // 按状态筛选
                Appointment.AppointmentStatus appointmentStatus = Appointment.AppointmentStatus.valueOf(status);
                appointments = appointmentService.getAppointmentsByUserIdAndStatus(userPrincipal.getId(), appointmentStatus, pageable);
            } else {
                // 获取所有预约
                appointments = appointmentService.getAppointmentsByUserId(userPrincipal.getId(), pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", appointments));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("无效的状态参数", 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取预约列表失败", 500));
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Optional<User> userOpt = userRepository.findById(userPrincipal.getId());
            
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("用户不存在", 404));
            }
            
            User user = userOpt.get();

            // 管理员：可查看任意预约详情（不受 userId 限制）
            if (user.getRole() == User.Role.ADMIN) {
                Optional<Appointment> appointmentOpt = appointmentService.getAppointmentById(id);
                if (!appointmentOpt.isPresent()) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("预约不存在", 404));
                }
                return ResponseEntity.ok(ApiResponse.success("获取成功", appointmentOpt.get()));
            }

            // 普通用户：只能查看自己的预约
            if (user.getRole() == User.Role.USER) {
                Optional<Appointment> appointmentOpt = appointmentService.getAppointmentByIdAndUserId(id, userPrincipal.getId());
                if (!appointmentOpt.isPresent()) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("预约不存在", 404));
                }
                return ResponseEntity.ok(ApiResponse.success("获取成功", appointmentOpt.get()));
            }
            
            // 如果是员工，需要通过employeeId查询预约
            Optional<Appointment> employeeAppointmentOpt = appointmentService.getAppointmentById(id);
            if (!employeeAppointmentOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("预约不存在", 404));
            }
            
            Appointment appointment = employeeAppointmentOpt.get();
            // 检查预约是否分配给该员工
            if (appointment.getEmployeeId() == null || !appointment.getEmployeeId().equals(user.getEmployeeId())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("没有权限访问", 403));
            }
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", appointment));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取预约详情失败", 500));
        }
    }
    
    @GetMapping("/order/{orderNo}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAppointmentByOrderNo(@PathVariable String orderNo) {
        try {
            Optional<Appointment> appointmentOpt = appointmentService.getAppointmentByOrderNo(orderNo);
            
            if (!appointmentOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("预约不存在", 404));
            }
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", appointmentOpt.get()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取预约详情失败", 500));
        }
    }
    
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            appointmentService.cancelAppointment(id, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success("取消成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("取消预约失败", 500));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            appointmentService.deleteAppointment(id, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success("删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除预约失败", 500));
        }
    }
    
    // 管理员功能
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAppointmentStatus(@PathVariable Long id, 
                                                   @RequestParam Appointment.AppointmentStatus status) {
        try {
            Appointment appointment = appointmentService.updateAppointmentStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("状态更新成功", appointment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新状态失败", 500));
        }
    }
    
    @PutMapping("/{id}/payment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Long id, 
                                               @RequestParam Appointment.PaymentStatus paymentStatus) {
        try {
            Appointment appointment = appointmentService.updatePaymentStatus(id, paymentStatus);
            return ResponseEntity.ok(ApiResponse.success("支付状态更新成功", appointment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新支付状态失败", 500));
        }
    }
    
    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAppointmentsByStatus(@PathVariable Appointment.AppointmentStatus status,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Appointment> appointments = appointmentService.getAppointmentsByStatus(status, pageable);
            return ResponseEntity.ok(ApiResponse.success("获取成功", appointments));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取预约列表失败", 500));
        }
    }
    
    @GetMapping("/admin/today")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTodayAppointments() {
        try {
            return ResponseEntity.ok(ApiResponse.success("获取成功", appointmentService.getTodayAppointments()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取今日预约失败", 500));
        }
    }
    
    @GetMapping("/available-slots")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAvailableTimeSlots(@RequestParam String date,
                                                 @RequestParam Long serviceId) {
        try {
            return ResponseEntity.ok(ApiResponse.success("获取成功", appointmentService.getAvailableTimeSlots(date, serviceId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取可用时间段失败", 500));
        }
    }
    
    /**
     * 更新预约备注（员工专用）
     */
    @PutMapping("/{id}/notes")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> updateAppointmentNotes(@PathVariable Long id, 
                                                   @RequestBody Map<String, String> request,
                                                   Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Optional<User> userOpt = userRepository.findById(userPrincipal.getId());
            
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("用户不存在", 404));
            }
            
            User user = userOpt.get();
            Optional<Appointment> appointmentOpt = appointmentService.getAppointmentById(id);
            
            if (!appointmentOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("预约不存在", 404));
            }
            
            Appointment appointment = appointmentOpt.get();
            
            // 检查权限：只能编辑分配给该员工的预约
            if (appointment.getEmployeeId() == null || !appointment.getEmployeeId().equals(user.getEmployeeId())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("没有权限编辑此预约", 403));
            }
            
            // 更新备注
            String notes = request.get("notes");
            appointment.setNotes(notes);
            appointmentService.updateAppointment(appointment);
            
            return ResponseEntity.ok(ApiResponse.success("备注已更新", appointment));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新备注失败: " + e.getMessage(), 500));
        }
    }
}
