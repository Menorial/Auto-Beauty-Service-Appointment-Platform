package com.autobeauty.repository;

import com.autobeauty.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    /**
     * 根据用户ID查找预约列表
     */
    Page<Appointment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 根据用户ID和状态查找预约列表
     */
    Page<Appointment> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Appointment.AppointmentStatus status, Pageable pageable);
    
    /**
     * 根据订单号查找预约
     */
    Optional<Appointment> findByOrderNo(String orderNo);
    
    /**
     * 根据用户ID和预约ID查找预约
     */
    Optional<Appointment> findByIdAndUserId(Long id, Long userId);
    
    /**
     * 根据状态查找预约列表
     */
    Page<Appointment> findByStatusOrderByCreatedAtDesc(Appointment.AppointmentStatus status, Pageable pageable);
    
    /**
     * 根据状态查找预约列表（字符串参数）
     */
    Page<Appointment> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    
    /**
     * 查找所有预约按创建时间倒序
     */
    Page<Appointment> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 根据状态查找预约列表（字符串参数，返回List）
     */
    List<Appointment> findByStatus(String status);
    
    /**
     * 根据预约时间范围查找预约
     */
    List<Appointment> findByAppointmentTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查找指定时间段内的预约（用于检查时间冲突）
     */
    @Query("SELECT a FROM Appointment a WHERE a.appointmentTime BETWEEN :startTime AND :endTime AND a.status NOT IN ('CANCELLED')")
    List<Appointment> findConflictingAppointments(@Param("startTime") LocalDateTime startTime, 
                                                 @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据员工ID查找预约列表
     */
    List<Appointment> findByEmployeeIdAndAppointmentTimeBetween(Long employeeId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据服务ID查找预约列表
     */
    Page<Appointment> findByServiceIdOrderByCreatedAtDesc(Long serviceId, Pageable pageable);
    
    /**
     * 统计用户预约数量
     */
    long countByUserId(Long userId);
    
    /**
     * 统计指定状态的预约数量
     */
    long countByStatus(Appointment.AppointmentStatus status);
    
    /**
     * 查找今日预约
     */
    @Query("SELECT a FROM Appointment a WHERE DATE(a.appointmentTime) = CURRENT_DATE ORDER BY a.appointmentTime ASC")
    List<Appointment> findTodayAppointments();
    
    /**
     * 查找即将到期的预约（用于提醒）
     */
    @Query("SELECT a FROM Appointment a WHERE a.appointmentTime BETWEEN :startTime AND :endTime AND a.status = 'CONFIRMED'")
    List<Appointment> findUpcomingAppointments(@Param("startTime") LocalDateTime startTime, 
                                             @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据支付状态查找预约
     */
    List<Appointment> findByPaymentStatusOrderByCreatedAtDesc(Appointment.PaymentStatus paymentStatus);
    
    /**
     * 统计收入（已支付的预约）
     */
    @Query("SELECT SUM(a.totalAmount) FROM Appointment a WHERE a.paymentStatus = 'PAID' AND a.paymentTime BETWEEN :startTime AND :endTime")
    java.math.BigDecimal calculateRevenue(@Param("startTime") LocalDateTime startTime, 
                                        @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据员工ID查找预约列表（分页）
     */
    Page<Appointment> findByEmployeeId(Long employeeId, Pageable pageable);
    
    /**
     * 根据员工ID和状态查找预约列表（分页）
     */
    @Query("SELECT a FROM Appointment a WHERE a.employeeId = :employeeId AND CAST(a.status AS string) = :status ORDER BY a.appointmentTime DESC")
    Page<Appointment> findByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, @Param("status") String status, Pageable pageable);
    
    /**
     * 统计员工的预约数量
     */
    long countByEmployeeId(Long employeeId);
    
    /**
     * 统计员工指定状态的预约数量
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.employeeId = :employeeId AND CAST(a.status AS string) = :status")
    long countByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, @Param("status") String status);
}
