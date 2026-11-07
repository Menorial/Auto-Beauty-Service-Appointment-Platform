package com.autobeauty.repository;

import com.autobeauty.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    /**
     * 根据用户ID查找车辆列表
     */
    List<Vehicle> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);
    
    /**
     * 根据用户ID和车辆ID查找车辆
     */
    Optional<Vehicle> findByIdAndUserId(Long id, Long userId);
    
    /**
     * 根据车牌号查找车辆
     */
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    
    /**
     * 检查车牌号是否存在
     */
    boolean existsByLicensePlate(String licensePlate);
    
    /**
     * 根据用户ID查找默认车辆
     */
    Optional<Vehicle> findByUserIdAndIsDefault(Long userId, Integer isDefault);
    
    /**
     * 清除用户的所有默认车辆设置
     */
    @Modifying
    @Query("UPDATE Vehicle v SET v.isDefault = 0 WHERE v.userId = :userId")
    void clearDefaultVehicleByUserId(@Param("userId") Long userId);
    
    /**
     * 设置默认车辆
     */
    @Modifying
    @Query("UPDATE Vehicle v SET v.isDefault = CASE WHEN v.id = :vehicleId THEN 1 ELSE 0 END WHERE v.userId = :userId")
    void setDefaultVehicle(@Param("userId") Long userId, @Param("vehicleId") Long vehicleId);
    
    /**
     * 统计用户车辆数量
     */
    long countByUserId(Long userId);
    
    /**
     * 根据品牌查找车辆
     */
    List<Vehicle> findByBrandContainingIgnoreCase(String brand);
}
