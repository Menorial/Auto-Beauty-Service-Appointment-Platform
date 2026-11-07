package com.autobeauty.controller;

import com.autobeauty.dto.ApiResponse;
import com.autobeauty.entity.Service;
import com.autobeauty.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/services")
public class ServiceController {
    
    @Autowired
    private ServiceService serviceService;
    
    @GetMapping
    public ResponseEntity<?> getAllServices(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            List<Service> services;
            
            // 根据参数决定调用哪个方法
            if (keyword != null && !keyword.isEmpty()) {
                services = serviceService.searchServicesByName(keyword);
            } else if (category != null && !category.isEmpty()) {
                services = serviceService.getServicesByCategory(category);
            } else {
                services = serviceService.getAllActiveServices();
            }
            
            // 根据sort参数排序
            if (sort != null && !sort.isEmpty()) {
                services = sortServices(services, sort);
            }
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", services));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取服务列表失败", 500));
        }
    }
    
    // 排序辅助方法
    private List<Service> sortServices(List<Service> services, String sort) {
        switch (sort) {
            case "price_asc":
                services.sort((s1, s2) -> s1.getPrice().compareTo(s2.getPrice()));
                break;
            case "price_desc":
                services.sort((s1, s2) -> s2.getPrice().compareTo(s1.getPrice()));
                break;
            case "rating_desc":
                // 暂时不处理评分排序，因为需要关联查询
                break;
            case "popular":
                // 暂时不处理热门排序
                break;
            default:
                // 默认排序
                break;
        }
        return services;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getServiceById(@PathVariable Long id) {
        try {
            Optional<Service> serviceOpt = serviceService.getServiceById(id);
            
            if (!serviceOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("服务不存在", 404));
            }
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", serviceOpt.get()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取服务详情失败", 500));
        }
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getServicesByCategory(@PathVariable String category) {
        try {
            List<Service> services = serviceService.getServicesByCategory(category);
            return ResponseEntity.ok(ApiResponse.success("获取成功", services));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取分类服务失败", 500));
        }
    }
    
    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        try {
            List<String> categories = serviceService.getAllCategories();
            return ResponseEntity.ok(ApiResponse.success("获取成功", categories));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取服务分类失败", 500));
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchServices(@RequestParam String keyword) {
        try {
            List<Service> services = serviceService.searchServicesByName(keyword);
            return ResponseEntity.ok(ApiResponse.success("搜索成功", services));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("搜索服务失败", 500));
        }
    }
    
    @GetMapping("/price-range")
    public ResponseEntity<?> getServicesByPriceRange(@RequestParam BigDecimal minPrice, 
                                                    @RequestParam BigDecimal maxPrice) {
        try {
            List<Service> services = serviceService.getServicesByPriceRange(minPrice, maxPrice);
            return ResponseEntity.ok(ApiResponse.success("获取成功", services));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取价格区间服务失败", 500));
        }
    }
    
    @GetMapping("/popular")
    public ResponseEntity<?> getPopularServices() {
        try {
            List<Service> services = serviceService.getPopularServices();
            return ResponseEntity.ok(ApiResponse.success("获取成功", services));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取热门服务失败", 500));
        }
    }
    
    @GetMapping("/recommended")
    public ResponseEntity<?> getRecommendedServices() {
        try {
            List<Service> services = serviceService.getRecommendedServices();
            return ResponseEntity.ok(ApiResponse.success("获取成功", services));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取推荐服务失败", 500));
        }
    }
    
    // 管理员功能
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createService(@Valid @RequestBody Service service) {
        try {
            Service createdService = serviceService.createService(service);
            return ResponseEntity.ok(ApiResponse.success("创建成功", createdService));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建服务失败", 500));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateService(@PathVariable Long id, @Valid @RequestBody Service serviceUpdate) {
        try {
            serviceUpdate.setId(id);
            Service updatedService = serviceService.updateService(serviceUpdate);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updatedService));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新服务失败", 500));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        try {
            serviceService.deleteService(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除服务失败", 500));
        }
    }
    
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllServicesForAdmin() {
        try {
            List<Service> services = serviceService.getAllServices();
            return ResponseEntity.ok(ApiResponse.success("获取成功", services));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取服务列表失败", 500));
        }
    }
}
