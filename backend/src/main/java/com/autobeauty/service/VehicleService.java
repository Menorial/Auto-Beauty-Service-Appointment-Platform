package com.autobeauty.service;

import com.autobeauty.entity.Vehicle;
import com.autobeauty.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VehicleService {
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    /**
     * 添加车辆
     */
    public Vehicle addVehicle(Vehicle vehicle) {
        // 检查车牌号是否已存在
        if (vehicleRepository.existsByLicensePlate(vehicle.getLicensePlate())) {
            throw new RuntimeException("车牌号已存在");
        }
        
        // 如果是第一辆车，自动设为默认车辆
        List<Vehicle> userVehicles = vehicleRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(vehicle.getUserId());
        if (userVehicles.isEmpty()) {
            vehicle.setIsDefault(1);
        }
        
        return vehicleRepository.save(vehicle);
    }

    /**
     * 根据用户ID获取车辆列表
     */
    public List<Vehicle> getVehiclesByUserId(Long userId) {
        return vehicleRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
    }

    /**
     * 根据ID和用户ID获取车辆
     */
    public Optional<Vehicle> getVehicleByIdAndUserId(Long id, Long userId) {
        return vehicleRepository.findByIdAndUserId(id, userId);
    }

    /**
     * 更新车辆信息
     */
    public Vehicle updateVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    /**
     * 删除车辆
     */
    public void deleteVehicle(Long id, Long userId) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findByIdAndUserId(id, userId);
        if (vehicleOpt.isPresent()) {
            Vehicle vehicle = vehicleOpt.get();
            vehicleRepository.delete(vehicle);
            
            // 如果删除的是默认车辆，需要重新设置默认车辆
            if (vehicle.getIsDefault() == 1) {
                List<Vehicle> remainingVehicles = vehicleRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
                if (!remainingVehicles.isEmpty()) {
                    Vehicle newDefault = remainingVehicles.get(0);
                    newDefault.setIsDefault(1);
                    vehicleRepository.save(newDefault);
                }
            }
        } else {
            throw new RuntimeException("车辆不存在或无权限删除");
        }
    }

    /**
     * 设置默认车辆
     */
    public void setDefaultVehicle(Long vehicleId, Long userId) {
        // 检查车辆是否属于该用户
        Optional<Vehicle> vehicleOpt = vehicleRepository.findByIdAndUserId(vehicleId, userId);
        if (!vehicleOpt.isPresent()) {
            throw new RuntimeException("车辆不存在或无权限操作");
        }
        
        // 清除用户的所有默认车辆设置，然后设置新的默认车辆
        vehicleRepository.setDefaultVehicle(userId, vehicleId);
    }

    /**
     * 根据用户ID获取默认车辆
     */
    public Optional<Vehicle> getDefaultVehicleByUserId(Long userId) {
        return vehicleRepository.findByUserIdAndIsDefault(userId, 1);
    }

    /**
     * 统计用户车辆数量
     */
    public long countVehiclesByUserId(Long userId) {
        return vehicleRepository.countByUserId(userId);
    }

    /**
     * 根据车牌号查找车辆
     */
    public Optional<Vehicle> getVehicleByLicensePlate(String licensePlate) {
        return vehicleRepository.findByLicensePlate(licensePlate);
    }
}
