package com.autobeauty.service;

import com.autobeauty.dto.RegisterRequest;
import com.autobeauty.entity.PointRecord;
import com.autobeauty.entity.User;
import com.autobeauty.repository.PointRecordRepository;
import com.autobeauty.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private PointRecordRepository pointRecordRepository;
    
    /**
     * 用户注册
     */
    public User registerUser(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (registerRequest.getEmail() != null && userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        // 检查手机号是否已存在
        if (registerRequest.getPhone() != null && userRepository.existsByPhone(registerRequest.getPhone())) {
            throw new RuntimeException("手机号已被注册");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setRealName(registerRequest.getRealName());
        user.setRole(User.Role.USER);
        user.setStatus(1);
        user.setPoints(0);
        
        return userRepository.save(user);
    }

    /**
     * 根据用户名查找用户
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 根据用户名或邮箱查找用户
     */
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail);
    }

    /**
     * 根据ID查找用户
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * 更新用户信息
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * 更新用户积分（带积分记录）
     */
    public void updateUserPoints(Long userId, Integer points, String reason, Long relatedId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPoints(user.getPoints() + points);
            userRepository.save(user);
            
            // 创建积分记录
            PointRecord pointRecord = new PointRecord();
            pointRecord.setUserId(userId);
            pointRecord.setType(points > 0 ? PointRecord.PointType.EARN : PointRecord.PointType.SPEND);
            pointRecord.setPoints(Math.abs(points));
            pointRecord.setReason(reason);
            pointRecord.setRelatedId(relatedId);
            pointRecordRepository.save(pointRecord);
        }
    }

    /**
     * 更新用户积分（不带relatedId）
     */
    public void updateUserPoints(Long userId, Integer points, String reason) {
        updateUserPoints(userId, points, reason, null);
    }

    /**
     * 更新用户积分（保持向后兼容，默认原因）
     */
    public void updateUserPoints(Long userId, Integer points) {
        updateUserPoints(userId, points, "积分变更", null);
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 根据状态获取用户列表
     */
    public List<User> getUsersByStatus(Integer status) {
        return userRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    /**
     * 统计活跃用户数量
     */
    public long countActiveUsers() {
        return userRepository.countActiveUsers();
    }
    
    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * 检查邮箱是否存在
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * 检查手机号是否存在
     */
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }
    
    /**
     * 修改用户密码
     */
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        // 验证旧密码是否正确
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("当前密码不正确");
        }
        
        // 更新为新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return true;
    }
}
