package com.autobeauty.repository;

import com.autobeauty.entity.PointRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointRecordRepository extends JpaRepository<PointRecord, Long> {
    
    /**
     * 根据用户ID查找积分记录
     */
    Page<PointRecord> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 根据用户ID和类型查找积分记录
     */
    List<PointRecord> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, PointRecord.PointType type);
    
    /**
     * 计算用户积分总和
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN pr.type = 'EARN' THEN pr.points ELSE -pr.points END), 0) FROM PointRecord pr WHERE pr.userId = :userId")
    Integer calculateUserTotalPoints(@Param("userId") Long userId);
    
    /**
     * 根据时间范围查找积分记录
     */
    List<PointRecord> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long userId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计用户获得的积分
     */
    @Query("SELECT COALESCE(SUM(pr.points), 0) FROM PointRecord pr WHERE pr.userId = :userId AND pr.type = 'EARN'")
    Integer calculateUserEarnedPoints(@Param("userId") Long userId);
    
    /**
     * 统计用户消费的积分
     */
    @Query("SELECT COALESCE(SUM(pr.points), 0) FROM PointRecord pr WHERE pr.userId = :userId AND pr.type = 'SPEND'")
    Integer calculateUserSpentPoints(@Param("userId") Long userId);
}
