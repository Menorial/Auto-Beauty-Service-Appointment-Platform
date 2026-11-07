package com.autobeauty.repository;

import com.autobeauty.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    
    /**
     * 查找启用的服务列表，按排序和创建时间排序
     */
    List<Service> findByStatusOrderBySortOrderAscCreatedAtDesc(Integer status);
    
    /**
     * 根据分类查找服务列表
     */
    List<Service> findByCategoryAndStatusOrderBySortOrderAscCreatedAtDesc(String category, Integer status);
    
    /**
     * 根据服务名称模糊查询
     */
    List<Service> findByNameContainingIgnoreCaseAndStatus(String name, Integer status);
    
    /**
     * 查找所有分类
     */
    @Query("SELECT DISTINCT s.category FROM Service s WHERE s.status = 1 AND s.category IS NOT NULL ORDER BY s.category")
    List<String> findAllCategories();
    
    /**
     * 根据价格范围查找服务
     */
    @Query("SELECT s FROM Service s WHERE s.status = :status AND s.price BETWEEN :minPrice AND :maxPrice ORDER BY s.sortOrder ASC, s.createdAt DESC")
    List<Service> findByPriceRange(@Param("status") Integer status, 
                                  @Param("minPrice") java.math.BigDecimal minPrice, 
                                  @Param("maxPrice") java.math.BigDecimal maxPrice);
    
    /**
     * 查找热门服务（根据预约数量排序）
     */
    @Query("SELECT s FROM Service s LEFT JOIN s.appointments a WHERE s.status = 1 GROUP BY s.id ORDER BY COUNT(a) DESC")
    List<Service> findPopularServices();
    
    /**
     * 查找推荐服务（根据评分排序）
     */
    @Query("SELECT s FROM Service s LEFT JOIN s.reviews r WHERE s.status = 1 GROUP BY s.id ORDER BY AVG(r.rating) DESC NULLS LAST")
    List<Service> findRecommendedServices();
    
    /**
     * 统计启用的服务数量
     */
    long countByStatus(Integer status);
    
    /**
     * 根据ID和状态查找服务
     */
    Optional<Service> findByIdAndStatus(Long id, Integer status);

    /**
     * 获取指定状态的服务列表（管理员分页）
     */
    Page<Service> findByStatus(Integer status, Pageable pageable);
}
