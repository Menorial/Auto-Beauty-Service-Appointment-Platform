package com.autobeauty.dto;

import com.autobeauty.entity.User;

public class JwtResponse {
    
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String realName;
    private User.Role role;
    private String avatarUrl;
    private Long employeeId;
    
    public JwtResponse(String accessToken, Long id, String username, String email, String realName, User.Role role, String avatarUrl) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.realName = realName;
        this.role = role;
        this.avatarUrl = avatarUrl;
    }
    
    public JwtResponse(String accessToken, Long id, String username, String email, String realName, User.Role role, String avatarUrl, Long employeeId) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.realName = realName;
        this.role = role;
        this.avatarUrl = avatarUrl;
        this.employeeId = employeeId;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRealName() {
        return realName;
    }
    
    public void setRealName(String realName) {
        this.realName = realName;
    }
    
    public User.Role getRole() {
        return role;
    }
    
    public void setRole(User.Role role) {
        this.role = role;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public Long getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }
}
