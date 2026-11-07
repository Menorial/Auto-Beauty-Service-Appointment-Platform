package com.autobeauty.service;

import com.autobeauty.entity.Service;
import com.autobeauty.repository.ServiceRepository;
import com.autobeauty.util.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
@Transactional
public class ServiceService {
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    /**
     * 获取所有启用的服务
     */
    public List<Service> getAllActiveServices() {
        return serviceRepository.findByStatusOrderBySortOrderAscCreatedAtDesc(1);
    }
    
    /**
     * 根据分类获取服务列表
     */
    public List<Service> getServicesByCategory(String category) {
        return serviceRepository.findByCategoryAndStatusOrderBySortOrderAscCreatedAtDesc(category, 1);
    }
    
    /**
     * 根据ID获取服务详情
     */
    public Optional<Service> getServiceById(Long id) {
        return serviceRepository.findByIdAndStatus(id, 1);
    }
    
    /**
     * 根据服务名称搜索
     */
    public List<Service> searchServicesByName(String name) {
        return serviceRepository.findByNameContainingIgnoreCaseAndStatus(name, 1);
    }
    
    /**
     * 获取所有服务分类
     */
    public List<String> getAllCategories() {
        return serviceRepository.findAllCategories();
    }
    
    /**
     * 根据价格范围查找服务
     */
    public List<Service> getServicesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return serviceRepository.findByPriceRange(1, minPrice, maxPrice);
    }
    
    /**
     * 获取热门服务
     */
    public List<Service> getPopularServices() {
        return serviceRepository.findPopularServices();
    }
    
    /**
     * 获取推荐服务
     */
    public List<Service> getRecommendedServices() {
        return serviceRepository.findRecommendedServices();
    }

    /**
     * 创建服务项目（管理员功能）
     */
    public Service createService(Service service) {
        service.setStatus(1);
        
        // 处理Base64图片数据
        if (service.getImageUrl() != null && ImageUtils.isDataUrl(service.getImageUrl())) {
            byte[] imageData = ImageUtils.decodeDataUrl(service.getImageUrl());
            if (imageData != null) {
                service.setImageData(imageData);
                // 保持imageUrl为Base64格式，前端可以直接显示
            }
        }
        
        return serviceRepository.save(service);
    }

    /**
     * 更新服务项目（管理员功能）
     */
    public Service updateService(Service service) {
        // 如果传入的是完整对象，直接保存
        if (service.getId() != null && service.getName() != null && service.getPrice() != null) {
            return serviceRepository.save(service);
        }
        
        // 否则进行部分更新
        if (service.getId() == null) {
            throw new RuntimeException("服务ID不能为空");
        }
        
        Optional<Service> existingServiceOpt = serviceRepository.findById(service.getId());
        if (!existingServiceOpt.isPresent()) {
            throw new RuntimeException("服务不存在");
        }
        
        Service existingService = existingServiceOpt.get();
        
        // 只更新非空字段
        if (service.getName() != null && !service.getName().trim().isEmpty()) {
            existingService.setName(service.getName());
        }
        if (service.getDescription() != null) {
            existingService.setDescription(service.getDescription());
        }
        if (service.getPrice() != null) {
            existingService.setPrice(service.getPrice());
        }
        if (service.getDuration() != null) {
            existingService.setDuration(service.getDuration());
        }
        if (service.getCategory() != null) {
            existingService.setCategory(service.getCategory());
        }
        if (service.getImageUrl() != null) {
            // 处理Base64图片数据
            if (ImageUtils.isDataUrl(service.getImageUrl())) {
                byte[] imageData = ImageUtils.decodeDataUrl(service.getImageUrl());
                if (imageData != null) {
                    existingService.setImageData(imageData);
                    existingService.setImageUrl(service.getImageUrl()); // 保持Base64格式
                }
            } else {
                existingService.setImageUrl(service.getImageUrl());
            }
        }
        if (service.getSortOrder() != null) {
            existingService.setSortOrder(service.getSortOrder());
        }
        if (service.getStatus() != null) {
            existingService.setStatus(service.getStatus());
        }
        
        return serviceRepository.save(existingService);
    }

    /**
     * 删除服务项目（管理员功能）
     */
    public void deleteService(Long id) {
        Optional<Service> serviceOpt = serviceRepository.findById(id);
        if (serviceOpt.isPresent()) {
            Service service = serviceOpt.get();
            service.setStatus(0); // 软删除
            serviceRepository.save(service);
        } else {
            throw new RuntimeException("服务项目不存在");
        }
    }

    /**
     * 获取所有服务（管理员功能）
     */
    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    /**
     * 统计启用的服务数量
     */
    public long countActiveServices() {
        return serviceRepository.countByStatus(1);
    }
}
