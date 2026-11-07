package com.autobeauty.controller;

import com.autobeauty.dto.ApiResponse;
import com.autobeauty.entity.Review;
import com.autobeauty.service.ReviewService;
import com.autobeauty.service.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createReview(@RequestBody Review review, Authentication authentication) {
        try {
            // 从认证信息中获取用户ID
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            review.setUserId(userPrincipal.getId());
            
            // 验证必填字段
            if (review.getAppointmentId() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("预约订单ID不能为空", 400));
            }
            if (review.getServiceId() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("服务ID不能为空", 400));
            }
            if (review.getRating() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("评分不能为空", 400));
            }
            if (review.getRating() < 1 || review.getRating() > 5) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("评分必须在1-5之间", 400));
            }
            
            Review createdReview = reviewService.createReview(review);
            return ResponseEntity.ok(ApiResponse.success("评价成功", createdReview));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建评价失败", 500));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateReview(@PathVariable Long id, 
                                         @RequestBody Review reviewUpdate,
                                         Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            // 验证评分范围
            if (reviewUpdate.getRating() != null && 
                (reviewUpdate.getRating() < 1 || reviewUpdate.getRating() > 5)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("评分必须在1-5之间", 400));
            }
            
            Review updatedReview = reviewService.updateReview(id, reviewUpdate, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success("评价更新成功", updatedReview));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新评价失败", 500));
        }
    }
    
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<?> getReviewsByServiceId(@PathVariable Long serviceId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Review> reviews = reviewService.getReviewsByServiceId(serviceId, pageable);
            return ResponseEntity.ok(ApiResponse.success("获取成功", reviews));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取评价列表失败", 500));
        }
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getReviewsByUserId(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            // 只能查看自己的评价，除非是管理员
            if (!userPrincipal.getId().equals(userId) && 
                !userPrincipal.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("无权限查看", 403));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Review> reviews = reviewService.getReviewsByUserId(userId, pageable);
            return ResponseEntity.ok(ApiResponse.success("获取成功", reviews));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取评价列表失败", 500));
        }
    }
    
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getReviewByAppointmentId(@PathVariable Long appointmentId) {
        try {
            Optional<Review> reviewOpt = reviewService.getReviewByAppointmentId(appointmentId);
            
            if (!reviewOpt.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("暂无评价", null));
            }
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", reviewOpt.get()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取评价失败", 500));
        }
    }
    
    @PostMapping("/{id}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> replyToReview(@PathVariable Long id, @RequestParam String reply) {
        try {
            Review review = reviewService.replyToReview(id, reply);
            return ResponseEntity.ok(ApiResponse.success("回复成功", review));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("回复失败", 500));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteReview(@PathVariable Long id, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            boolean isAdmin = userPrincipal.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            reviewService.deleteReviewByUser(id, userPrincipal.getId(), isAdmin);
            return ResponseEntity.ok(ApiResponse.success("删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除评价失败", 500));
        }
    }
    
    @GetMapping("/service/{serviceId}/rating")
    public ResponseEntity<?> getServiceRating(@PathVariable Long serviceId) {
        try {
            Double avgRating = reviewService.calculateAverageRating(serviceId);
            long reviewCount = reviewService.countReviewsByServiceId(serviceId);
            List<Object[]> ratingDistribution = reviewService.getRatingDistribution(serviceId);
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("averageRating", avgRating);
            result.put("reviewCount", reviewCount);
            result.put("ratingDistribution", ratingDistribution);
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取评分信息失败", 500));
        }
    }
    
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestReviews() {
        try {
            List<com.autobeauty.dto.ReviewResponse> reviews = reviewService.getLatestReviews();
            return ResponseEntity.ok(ApiResponse.success("获取成功", reviews));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取最新评价失败", 500));
        }
    }
    
    @GetMapping("/high-rating")
    public ResponseEntity<?> getHighRatingReviews() {
        try {
            List<com.autobeauty.dto.ReviewResponse> reviews = reviewService.getHighRatingReviews();
            return ResponseEntity.ok(ApiResponse.success("获取成功", reviews));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取高分评价失败", 500));
        }
    }
    
    @GetMapping("/admin/unreplied")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUnrepliedReviews() {
        try {
            List<Review> reviews = reviewService.getUnrepliedReviews();
            return ResponseEntity.ok(ApiResponse.success("获取成功", reviews));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取未回复评价失败", 500));
        }
    }
}
