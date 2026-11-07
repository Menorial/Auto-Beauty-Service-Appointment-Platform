package com.autobeauty.controller;

import com.autobeauty.dto.*;
import com.autobeauty.entity.User;
import com.autobeauty.repository.UserRepository;
import com.autobeauty.service.CaptchaService;
import com.autobeauty.service.UserService;
import com.autobeauty.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

import org.springframework.util.StringUtils;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    AuthenticationManager authenticationManager;
    
    @Autowired
    UserService userService;
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    JwtUtils jwtUtils;
    
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    CaptchaService captchaService;

    /**
     * 获取登录/注册用验证码（服务端生成，提交时携带 captchaId + 用户输入的 captcha）
     */
    @GetMapping("/captcha")
    public ResponseEntity<?> getCaptcha() {
        CaptchaResponse captcha = captchaService.create();
        return ResponseEntity.ok(ApiResponse.success("获取成功", captcha));
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            ResponseEntity<?> captchaCheck = verifyCaptchaIfPresent(
                    loginRequest.getCaptchaId(), loginRequest.getCaptcha());
            if (captchaCheck != null) {
                return captchaCheck;
            }

            // 先检查用户是否存在和状态
            Optional<User> userOpt = userService.findByUsernameOrEmail(loginRequest.getUsername());
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("用户名或密码错误", 400));
            }
            
            User user = userOpt.get();
            
            // 检查用户状态
            if (user.getStatus() == null || user.getStatus() == 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("该账号已被禁用，请联系管理员", 403));
            }
            
            // 进行密码验证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            JwtResponse jwtResponse = new JwtResponse(jwt, user.getId(), user.getUsername(), 
                    user.getEmail(), user.getRealName(), user.getRole(), user.getAvatarUrl(), user.getEmployeeId());
            
            return ResponseEntity.ok(ApiResponse.success("登录成功", jwtResponse));
        } catch (UsernameNotFoundException e) {
            // 用户不存在
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("用户名或密码错误", 400));
        } catch (BadCredentialsException e) {
            // 密码错误
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("用户名或密码错误", 400));
        } catch (Exception e) {
            // 其他异常
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("登录失败，请稍后重试", 400));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            ResponseEntity<?> captchaCheck = verifyCaptchaIfPresent(
                    registerRequest.getCaptchaId(), registerRequest.getCaptcha());
            if (captchaCheck != null) {
                return captchaCheck;
            }

            User user = userService.registerUser(registerRequest);
            return ResponseEntity.ok(ApiResponse.success("注册成功", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("注册失败", 500));
        }
    }
    
    @PostMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(ApiResponse.success("检查完成", !exists));
    }
    
    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("检查完成", !exists));
    }
    
    @PostMapping("/check-phone")
    public ResponseEntity<?> checkPhone(@RequestParam String phone) {
        boolean exists = userService.existsByPhone(phone);
        return ResponseEntity.ok(ApiResponse.success("检查完成", !exists));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // JWT是无状态的，注销主要在前端处理（删除token）
        // 这里只需要返回成功响应
        return ResponseEntity.ok(ApiResponse.success("注销成功", null));
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            // 验证两次输入的密码是否一致
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("两次输入的密码不一致", 400));
            }
            
            // 验证新密码与旧密码是否相同
            if (request.getOldPassword().equals(request.getNewPassword())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("新密码不能与当前密码相同", 400));
            }
            
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Optional<User> userOpt = userService.findByUsernameOrEmail(username);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("用户不存在", 404));
            }
            
            User user = userOpt.get();
            
            // 验证旧密码是否正确
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("当前密码错误", 400));
            }
            
            // 更新密码
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            
            return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("修改密码失败: " + e.getMessage(), 500));
        }
    }

    /**
     * 验证码：与旧版前端兼容。
     * <ul>
     *   <li>两者都为空：不校验（旧页面只有账号密码）</li>
     *   <li>只填其一：返回错误</li>
     *   <li>两者都有：必须校验通过</li>
     * </ul>
     *
     * @return null 表示通过；非 null 为需直接返回的错误响应
     */
    private ResponseEntity<?> verifyCaptchaIfPresent(String captchaId, String captcha) {
        boolean idEmpty = !StringUtils.hasText(captchaId);
        boolean codeEmpty = !StringUtils.hasText(captcha);
        if (idEmpty && codeEmpty) {
            return null;
        }
        if (idEmpty || codeEmpty) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("请先获取验证码并完整填写", 400));
        }
        if (!captchaService.validateAndConsume(captchaId.trim(), captcha.trim())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("验证码错误或已过期", 400));
        }
        return null;
    }
}
