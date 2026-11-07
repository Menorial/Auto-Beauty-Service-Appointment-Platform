package com.autobeauty.dto;

import com.autobeauty.entity.Appointment;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AppointmentResponse {
    
    private Long id;
    private String orderNo;
    private Long userId;
    private Long vehicleId;
    private Long serviceId;
    private Long employeeId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appointmentTime;
    
    private Appointment.AppointmentStatus status;
    private BigDecimal totalAmount;
    private Appointment.PaymentStatus paymentStatus;
    private String paymentMethod;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;
    
    private String notes;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // 关联对象信息
    private UserInfo user;
    private VehicleInfo vehicle;
    private ServiceInfo service;
    private EmployeeInfo employee;
    
    // 内部类
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String phone;
        private String email;
        
        public UserInfo() {}
        
        public UserInfo(Long id, String username, String realName, String phone, String email) {
            this.id = id;
            this.username = username;
            this.realName = realName;
            this.phone = phone;
            this.email = email;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class VehicleInfo {
        private Long id;
        private String brand;
        private String model;
        private String licensePlate;
        private String color;
        
        public VehicleInfo() {}
        
        public VehicleInfo(Long id, String brand, String model, String licensePlate, String color) {
            this.id = id;
            this.brand = brand;
            this.model = model;
            this.licensePlate = licensePlate;
            this.color = color;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getLicensePlate() { return licensePlate; }
        public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }
    
    public static class ServiceInfo {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer duration;
        private String category;
        
        public ServiceInfo() {}
        
        public ServiceInfo(Long id, String name, String description, BigDecimal price, Integer duration, String category) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.duration = duration;
            this.category = category;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public Integer getDuration() { return duration; }
        public void setDuration(Integer duration) { this.duration = duration; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
    
    public static class EmployeeInfo {
        private Long id;
        private String name;
        private String phone;
        private String specialties;
        
        public EmployeeInfo() {}
        
        public EmployeeInfo(Long id, String name, String phone, String specialties) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.specialties = specialties;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getSpecialties() { return specialties; }
        public void setSpecialties(String specialties) { this.specialties = specialties; }
    }
    
    // 构造函数
    public AppointmentResponse() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    
    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }
    
    public Appointment.AppointmentStatus getStatus() { return status; }
    public void setStatus(Appointment.AppointmentStatus status) { this.status = status; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public Appointment.PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(Appointment.PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }
    
    public VehicleInfo getVehicle() { return vehicle; }
    public void setVehicle(VehicleInfo vehicle) { this.vehicle = vehicle; }
    
    public ServiceInfo getService() { return service; }
    public void setService(ServiceInfo service) { this.service = service; }
    
    public EmployeeInfo getEmployee() { return employee; }
    public void setEmployee(EmployeeInfo employee) { this.employee = employee; }
}
