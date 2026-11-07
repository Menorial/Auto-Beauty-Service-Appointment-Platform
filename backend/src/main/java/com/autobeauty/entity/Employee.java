package com.autobeauty.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "employees")
@EntityListeners(AuditingEntityListener.class)
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "员工姓名不能为空")
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 200)
    private String skills;
    
    @Column(name = "work_schedule", length = 100)
    private String workSchedule;
    
    @Column(name = "avatar_url", columnDefinition = "MEDIUMTEXT")
    private String avatarUrl;
    
    @Lob
    @Column(name = "avatar_data", columnDefinition = "LONGBLOB")
    @JsonIgnore
    private byte[] avatarData;
    
    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status; // 0-离职，1-在职
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 关联关系
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Appointment> appointments;
    
    // 构造函数
    public Employee() {}
    
    public Employee(String name, String phone, String skills) {
        this.name = name;
        this.phone = phone;
        this.skills = skills;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getSkills() {
        return skills;
    }
    
    public void setSkills(String skills) {
        this.skills = skills;
    }
    
    public String getWorkSchedule() {
        return workSchedule;
    }
    
    public void setWorkSchedule(String workSchedule) {
        this.workSchedule = workSchedule;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public byte[] getAvatarData() {
        return avatarData;
    }
    
    public void setAvatarData(byte[] avatarData) {
        this.avatarData = avatarData;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
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
    
    public List<Appointment> getAppointments() {
        return appointments;
    }
    
    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }
}
