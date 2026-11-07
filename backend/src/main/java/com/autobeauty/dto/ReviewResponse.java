package com.autobeauty.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class ReviewResponse {
    
    private Long id;
    private Long appointmentId;
    private Long userId;
    private Long serviceId;
    private Integer rating;
    private String comment;
    private String images;
    private String reply;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime replyTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
    
    // 关联信息
    private UserInfo user;
    private ServiceInfo service;
    
    // 内部类：用户信息
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String avatarUrl;
        
        public UserInfo() {}
        
        public UserInfo(Long id, String username, String realName, String avatarUrl) {
            this.id = id;
            this.username = username;
            this.realName = realName;
            this.avatarUrl = avatarUrl;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    }
    
    // 内部类：服务信息
    public static class ServiceInfo {
        private Long id;
        private String name;
        private String description;
        
        public ServiceInfo() {}
        
        public ServiceInfo(Long id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    // Constructors
    public ReviewResponse() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getServiceId() {
        return serviceId;
    }
    
    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }
    
    public Integer getRating() {
        return rating;
    }
    
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public String getImages() {
        return images;
    }
    
    public void setImages(String images) {
        this.images = images;
    }
    
    public String getReply() {
        return reply;
    }
    
    public void setReply(String reply) {
        this.reply = reply;
    }
    
    public LocalDateTime getReplyTime() {
        return replyTime;
    }
    
    public void setReplyTime(LocalDateTime replyTime) {
        this.replyTime = replyTime;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public UserInfo getUser() {
        return user;
    }
    
    public void setUser(UserInfo user) {
        this.user = user;
    }
    
    public ServiceInfo getService() {
        return service;
    }
    
    public void setService(ServiceInfo service) {
        this.service = service;
    }
}
