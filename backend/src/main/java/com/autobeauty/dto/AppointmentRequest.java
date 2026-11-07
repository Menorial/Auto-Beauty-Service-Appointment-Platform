package com.autobeauty.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AppointmentRequest {
    
    @NotNull(message = "车辆ID不能为空")
    private Long vehicleId;
    
    @NotNull(message = "服务ID不能为空")
    private Long serviceId;
    
    @NotNull(message = "预约时间不能为空")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime appointmentTime;
    
    private String notes;
    
    public AppointmentRequest() {}
    
    public AppointmentRequest(Long vehicleId, Long serviceId, LocalDateTime appointmentTime) {
        this.vehicleId = vehicleId;
        this.serviceId = serviceId;
        this.appointmentTime = appointmentTime;
    }
    
    public Long getVehicleId() {
        return vehicleId;
    }
    
    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }
    
    public Long getServiceId() {
        return serviceId;
    }
    
    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }
    
    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }
    
    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
