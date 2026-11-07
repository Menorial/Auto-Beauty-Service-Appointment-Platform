package com.autobeauty.repository;

import com.autobeauty.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    /**
     * 查找在职员工列表
     */
    List<Employee> findByStatusOrderByCreatedAtDesc(Integer status);
    
    /**
     * 根据姓名模糊查询员工
     */
    List<Employee> findByNameContainingIgnoreCaseAndStatus(String name, Integer status);
    
    /**
     * 根据技能查找员工
     */
    List<Employee> findBySkillsContainingIgnoreCaseAndStatus(String skills, Integer status);
    
    /**
     * 查找空闲员工（指定时间段内没有预约的员工）
     */
    @Query("SELECT e FROM Employee e WHERE e.status = 1 AND e.id NOT IN " +
           "(SELECT DISTINCT a.employeeId FROM Appointment a WHERE a.employeeId IS NOT NULL " +
           "AND a.appointmentTime BETWEEN :startTime AND :endTime AND a.status NOT IN ('CANCELLED'))")
    List<Employee> findAvailableEmployees(@Param("startTime") LocalDateTime startTime, 
                                        @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计在职员工数量
     */
    long countByStatus(Integer status);
    
    /**
     * 查找员工工作量统计
     */
    @Query("SELECT e.id, e.name, COUNT(a) as appointmentCount FROM Employee e " +
           "LEFT JOIN Appointment a ON e.id = a.employeeId " +
           "WHERE e.status = 1 AND (a.appointmentTime IS NULL OR a.appointmentTime BETWEEN :startTime AND :endTime) " +
           "GROUP BY e.id, e.name ORDER BY appointmentCount DESC")
    List<Object[]> findEmployeeWorkload(@Param("startTime") LocalDateTime startTime, 
                                      @Param("endTime") LocalDateTime endTime);
}
