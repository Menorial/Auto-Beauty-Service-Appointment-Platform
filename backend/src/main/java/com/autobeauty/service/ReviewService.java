package com.autobeauty.service;

import com.autobeauty.dto.ReviewResponse;
import com.autobeauty.entity.Review;
import com.autobeauty.entity.User;
import com.autobeauty.repository.ReviewRepository;
import com.autobeauty.repository.ServiceRepository;
import com.autobeauty.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    /**
     * 创建评价
     */
    public Review createReview(Review review) {
        // 检查是否已经评价过
        Optional<Review> existingReview = reviewRepository.findByAppointmentId(review.getAppointmentId());
        if (existingReview.isPresent()) {
            throw new RuntimeException("该预约已评价，不能重复评价");
        }
        
        Review savedReview = reviewRepository.save(review);
        
        // 给用户增加评价积分
        String reason = "完成服务评价获得积分";
        userService.updateUserPoints(review.getUserId(), 10, reason, review.getAppointmentId());
        
        return savedReview;
    }

    /**
     * 更新评价
     */
    public Review updateReview(Long reviewId, Review reviewUpdate, Long userId) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (!reviewOpt.isPresent()) {
            throw new RuntimeException("评价不存在");
        }
        
        Review review = reviewOpt.get();
        
        // 验证是否是评价的作者
        if (!review.getUserId().equals(userId)) {
            throw new RuntimeException("无权限修改此评价");
        }
        
        // 更新允许修改的字段
        if (reviewUpdate.getRating() != null) {
            if (reviewUpdate.getRating() < 1 || reviewUpdate.getRating() > 5) {
                throw new RuntimeException("评分必须在1-5之间");
            }
            review.setRating(reviewUpdate.getRating());
        }
        
        if (reviewUpdate.getComment() != null) {
            review.setComment(reviewUpdate.getComment());
        }
        
        if (reviewUpdate.getImages() != null) {
            review.setImages(reviewUpdate.getImages());
        }
        
        return reviewRepository.save(review);
    }

    /**
     * 根据服务ID获取评价列表
     */
    public Page<Review> getReviewsByServiceId(Long serviceId, Pageable pageable) {
        return reviewRepository.findByServiceIdOrderByCreatedAtDesc(serviceId, pageable);
    }

    /**
     * 根据用户ID获取评价列表
     */
    public Page<Review> getReviewsByUserId(Long userId, Pageable pageable) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 根据预约ID获取评价
     */
    public Optional<Review> getReviewByAppointmentId(Long appointmentId) {
        return reviewRepository.findByAppointmentId(appointmentId);
    }

    /**
     * 商家回复评价
     */
    public Review replyToReview(Long reviewId, String reply) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (!reviewOpt.isPresent()) {
            throw new RuntimeException("评价不存在");
        }
        
        Review review = reviewOpt.get();
        review.setReply(reply);
        review.setReplyTime(LocalDateTime.now());
        
        return reviewRepository.save(review);
    }

    /**
     * 计算服务的平均评分
     */
    public Double calculateAverageRating(Long serviceId) {
        Double avgRating = reviewRepository.calculateAverageRating(serviceId);
        return avgRating != null ? avgRating : 0.0;
    }

    /**
     * 统计服务的评价数量
     */
    public long countReviewsByServiceId(Long serviceId) {
        return reviewRepository.countByServiceId(serviceId);
    }

    /**
     * 获取评分分布
     */
    public List<Object[]> getRatingDistribution(Long serviceId) {
        return reviewRepository.getRatingDistribution(serviceId);
    }

    /**
     * 获取最新评价
     */
    public List<ReviewResponse> getLatestReviews() {
        List<Review> reviews = reviewRepository.findTop10ByOrderByCreatedAtDesc();
        return reviews.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取高分评价
     */
    public List<ReviewResponse> getHighRatingReviews() {
        List<Review> reviews = reviewRepository.findHighRatingReviews();
        return reviews.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 将Review实体转换为ReviewResponse
     */
    private ReviewResponse convertToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        
        response.setId(review.getId());
        response.setAppointmentId(review.getAppointmentId());
        response.setUserId(review.getUserId());
        response.setServiceId(review.getServiceId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        
        // 处理图片字段：将逗号分隔的路径转换为完整URL数组
        if (review.getImages() != null && !review.getImages().trim().isEmpty()) {
            // 保持原始格式，前端会自行处理
            response.setImages(review.getImages());
        } else {
            response.setImages(null);
        }
        
        response.setReply(review.getReply());
        response.setReplyTime(review.getReplyTime());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());
        
        // 加载用户信息
        if (review.getUserId() != null) {
            userRepository.findById(review.getUserId()).ifPresent(user -> {
                ReviewResponse.UserInfo userInfo = new ReviewResponse.UserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getRealName(),
                    user.getAvatarUrl()
                );
                response.setUser(userInfo);
            });
        }
        
        // 加载服务信息
        if (review.getServiceId() != null) {
            serviceRepository.findById(review.getServiceId()).ifPresent(service -> {
                ReviewResponse.ServiceInfo serviceInfo = new ReviewResponse.ServiceInfo(
                    service.getId(),
                    service.getName(),
                    service.getDescription()
                );
                response.setService(serviceInfo);
            });
        }
        
        return response;
    }
    
    /**
     * 获取未回复的评价
     */
    public List<Review> getUnrepliedReviews() {
        return reviewRepository.findByReplyIsNullOrderByCreatedAtDesc();
    }
    
    /**
     * 获取所有评价（管理员）- 返回ReviewResponse格式
     */
    public Page<ReviewResponse> getAllReviews(Pageable pageable) {
        Page<Review> reviewsPage = reviewRepository.findAll(pageable);
        List<ReviewResponse> reviewResponses = reviewsPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return new org.springframework.data.domain.PageImpl<>(
                reviewResponses, pageable, reviewsPage.getTotalElements());
    }
    
    /**
     * 根据条件搜索评价（管理员）- 返回ReviewResponse格式
     */
    public Page<ReviewResponse> searchReviews(String username, Long serviceId, Integer rating, String replyStatus, Pageable pageable) {
        // 获取所有评价
        List<Review> allReviews = reviewRepository.findAll();
        
        // 应用筛选条件
        List<Review> filteredReviews = allReviews.stream()
            .filter(review -> {
                // 按用户名筛选
                if (username != null && !username.trim().isEmpty()) {
                    Optional<User> userOpt = userRepository.findById(review.getUserId());
                    if (!userOpt.isPresent() || !userOpt.get().getUsername().contains(username)) {
                        return false;
                    }
                }
                
                // 按服务ID筛选
                if (serviceId != null && !review.getServiceId().equals(serviceId)) {
                    return false;
                }
                
                // 按评分筛选
                if (rating != null && !review.getRating().equals(rating)) {
                    return false;
                }
                
                // 按回复状态筛选
                if (replyStatus != null && !replyStatus.trim().isEmpty()) {
                    if ("REPLIED".equals(replyStatus) && review.getReply() == null) {
                        return false;
                    }
                    if ("UNREPLIED".equals(replyStatus) && review.getReply() != null) {
                        return false;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredReviews.size());
        List<Review> pageContent = filteredReviews.subList(start, end);
        
        // 转换为ReviewResponse
        List<ReviewResponse> reviewResponses = pageContent.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(
                reviewResponses, pageable, filteredReviews.size());
    }
    
    /**
     * 删除评价（管理员）
     */
    public void deleteReview(Long reviewId) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (!reviewOpt.isPresent()) {
            throw new RuntimeException("评价不存在");
        }
        reviewRepository.deleteById(reviewId);
    }

    /**
     * 删除评价（用户/管理员）
     */
    public void deleteReviewByUser(Long reviewId, Long userId, boolean isAdmin) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (!reviewOpt.isPresent()) {
            throw new RuntimeException("评价不存在");
        }

        Review review = reviewOpt.get();
        if (!isAdmin && !review.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此评价");
        }

        reviewRepository.deleteById(reviewId);
    }
}
