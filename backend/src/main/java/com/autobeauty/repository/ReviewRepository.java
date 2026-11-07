package com.autobeauty.repository;

import com.autobeauty.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    /**
     * 根据服务ID查找评价列表
     */
    Page<Review> findByServiceIdOrderByCreatedAtDesc(Long serviceId, Pageable pageable);
    
    /**
     * 根据用户ID查找评价列表
     */
    Page<Review> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 根据预约ID查找评价
     */
    Optional<Review> findByAppointmentId(Long appointmentId);
    
    /**
     * 根据评分查找评价
     */
    List<Review> findByRatingOrderByCreatedAtDesc(Integer rating);
    
    /**
     * 查找有商家回复的评价
     */
    List<Review> findByReplyIsNotNullOrderByReplyTimeDesc();
    
    /**
     * 查找未回复的评价
     */
    List<Review> findByReplyIsNullOrderByCreatedAtDesc();
    
    /**
     * 计算服务的平均评分
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.serviceId = :serviceId")
    Double calculateAverageRating(@Param("serviceId") Long serviceId);
    
    /**
     * 统计服务的评价数量
     */
    long countByServiceId(Long serviceId);
    
    /**
     * 根据评分统计服务评价分布
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.serviceId = :serviceId GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistribution(@Param("serviceId") Long serviceId);
    
    /**
     * 查找最新评价
     */
    List<Review> findTop10ByOrderByCreatedAtDesc();
    
    /**
     * 查找高分评价（4-5星）
     */
    @Query("SELECT r FROM Review r WHERE r.rating >= 4 ORDER BY r.createdAt DESC")
    List<Review> findHighRatingReviews();
    
    /**
     * 统计用户评价数量
     */
    long countByUserId(Long userId);
}
