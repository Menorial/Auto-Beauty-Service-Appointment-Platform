package com.autobeauty.repository;

import com.autobeauty.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据手机号查找用户
     */
    Optional<User> findByPhone(String phone);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);
    
    /**
     * 根据用户名或邮箱查找用户
     */
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);
    
    /**
     * 根据状态查找用户列表
     */
    @Query("SELECT u FROM User u WHERE u.status = :status ORDER BY u.createdAt DESC")
    java.util.List<User> findByStatusOrderByCreatedAtDesc(@Param("status") Integer status);
    
    /**
     * 根据角色查找用户列表
     */
    java.util.List<User> findByRole(User.Role role);

    /**
     * 根据员工ID查找对应的登录账号（role=EMPLOYEE 的用户）
     */
    Optional<User> findByEmployeeId(Long employeeId);
    
    /**
     * 统计用户总数
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 1")
    long countActiveUsers();
}
