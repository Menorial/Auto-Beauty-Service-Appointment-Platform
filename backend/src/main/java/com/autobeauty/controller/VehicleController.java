package com.autobeauty.controller;

import com.autobeauty.dto.ApiResponse;
import com.autobeauty.entity.Vehicle;
import com.autobeauty.service.UserPrincipal;
import com.autobeauty.service.VehicleService;
import com.autobeauty.util.LicensePlateValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/vehicles")
public class VehicleController {
    
    @Autowired
    private VehicleService vehicleService;
    
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserVehicles(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            List<Vehicle> vehicles = vehicleService.getVehiclesByUserId(userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success("获取成功", vehicles));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取车辆列表失败", 500));
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getVehicleById(@PathVariable Long id, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Optional<Vehicle> vehicleOpt = vehicleService.getVehicleByIdAndUserId(id, userPrincipal.getId());
            
            if (!vehicleOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("车辆不存在", 404));
            }
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", vehicleOpt.get()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取车辆信息失败", 500));
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> addVehicle(@Valid @RequestBody Vehicle vehicle, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            vehicle.setUserId(userPrincipal.getId());
            
            // 车牌号验证
            String validationError = LicensePlateValidator.validateWithMessage(
                vehicle.getLicensePlate(), vehicle.getVehicleType());
            if (validationError != null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(validationError, 400));
            }
            
            Vehicle savedVehicle = vehicleService.addVehicle(vehicle);
            return ResponseEntity.ok(ApiResponse.success("添加成功", savedVehicle));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("添加车辆失败", 500));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateVehicle(@PathVariable Long id, @Valid @RequestBody Vehicle vehicleUpdate, 
                                         Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Optional<Vehicle> vehicleOpt = vehicleService.getVehicleByIdAndUserId(id, userPrincipal.getId());
            
            if (!vehicleOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("车辆不存在或无权限操作", 404));
            }
            
            Vehicle vehicle = vehicleOpt.get();
            
            // 更新车辆信息
            if (vehicleUpdate.getLicensePlate() != null) {
                vehicle.setLicensePlate(vehicleUpdate.getLicensePlate());
            }
            if (vehicleUpdate.getVehicleType() != null) {
                vehicle.setVehicleType(vehicleUpdate.getVehicleType());
            }
            if (vehicleUpdate.getBrand() != null) {
                vehicle.setBrand(vehicleUpdate.getBrand());
            }
            if (vehicleUpdate.getModel() != null) {
                vehicle.setModel(vehicleUpdate.getModel());
            }
            if (vehicleUpdate.getColor() != null) {
                vehicle.setColor(vehicleUpdate.getColor());
            }
            if (vehicleUpdate.getYear() != null) {
                vehicle.setYear(vehicleUpdate.getYear());
            }
            if (vehicleUpdate.getImageUrl() != null) {
                vehicle.setImageUrl(vehicleUpdate.getImageUrl());
            }
            
            // 如果车牌号或车辆类型有更新，进行验证
            if (vehicleUpdate.getLicensePlate() != null || vehicleUpdate.getVehicleType() != null) {
                String validationError = LicensePlateValidator.validateWithMessage(
                    vehicle.getLicensePlate(), vehicle.getVehicleType());
                if (validationError != null) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error(validationError, 400));
                }
            }
            
            Vehicle updatedVehicle = vehicleService.updateVehicle(vehicle);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updatedVehicle));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新车辆信息失败", 500));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            vehicleService.deleteVehicle(id, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success("删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除车辆失败", 500));
        }
    }
    
    @PutMapping("/{id}/default")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> setDefaultVehicle(@PathVariable Long id, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            vehicleService.setDefaultVehicle(id, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success("设置成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("设置默认车辆失败", 500));
        }
    }
    
    @GetMapping("/default")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getDefaultVehicle(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Optional<Vehicle> vehicleOpt = vehicleService.getDefaultVehicleByUserId(userPrincipal.getId());
            
            if (!vehicleOpt.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("暂无默认车辆", null));
            }
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", vehicleOpt.get()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取默认车辆失败", 500));
        }
    }
}
