package com.autobeauty.controller;

import com.autobeauty.dto.ApiResponse;
import com.autobeauty.service.PaymentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制器
 */
@Api(tags = "支付管理")
@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * 创建支付宝支付订单
     */
    @ApiOperation("创建支付宝支付订单")
    @PostMapping("/alipay/create")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createAlipayOrder(@RequestParam Long appointmentId) {
        try {
            String form = paymentService.createAlipayOrder(appointmentId);
            return ResponseEntity.ok(ApiResponse.success("创建支付订单成功", form));
        } catch (Exception e) {
            logger.error("创建支付宝订单失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 支付宝支付回调（异步通知）
     */
    @ApiOperation("支付宝支付回调")
    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        try {
            // 获取支付宝POST过来的所有参数
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            
            for (String name : requestParams.keySet()) {
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }
            
            logger.info("收到支付宝回调: {}", params);
            
            // 处理回调
            boolean success = paymentService.handleAlipayNotify(params);
            
            if (success) {
                return "success";
            } else {
                return "fail";
            }
            
        } catch (Exception e) {
            logger.error("处理支付宝回调失败: {}", e.getMessage(), e);
            return "fail";
        }
    }
    
    /**
     * 支付宝支付同步返回
     */
    @ApiOperation("支付宝支付同步返回")
    @GetMapping("/alipay/return")
    public ResponseEntity<?> alipayReturn(HttpServletRequest request) {
        try {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            
            for (String name : requestParams.keySet()) {
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }
            
            logger.info("支付宝同步返回: {}", params);
            
            return ResponseEntity.ok(ApiResponse.success("支付成功", params));
        } catch (Exception e) {
            logger.error("处理支付宝同步返回失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 查询支付状态
     */
    @ApiOperation("查询支付状态")
    @GetMapping("/status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> queryPaymentStatus(@RequestParam Long appointmentId) {
        try {
            Map<String, Object> result = paymentService.queryPaymentStatus(appointmentId);
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            logger.error("查询支付状态失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
    
}
