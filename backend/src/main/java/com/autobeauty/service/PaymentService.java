package com.autobeauty.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.autobeauty.config.PaymentConfig;
import com.autobeauty.entity.Appointment;
import com.autobeauty.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 支付服务类
 */
@Service
@Transactional
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    @Autowired
    private AlipayClient alipayClient;
    
    @Autowired
    private PaymentConfig paymentConfig;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    /**
     * 创建支付宝支付订单
     */
    public String createAlipayOrder(Long appointmentId) throws Exception {
        // 获取预约信息
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (!appointmentOpt.isPresent()) {
            throw new RuntimeException("预约不存在");
        }
        
        Appointment appointment = appointmentOpt.get();
        
        // 检查支付状态
        if (appointment.getPaymentStatus() == Appointment.PaymentStatus.PAID) {
            throw new RuntimeException("订单已支付，请勿重复支付");
        }
        
        try {
            // 创建支付请求
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            
            // 设置异步回调地址
            request.setNotifyUrl(paymentConfig.getNotifyUrl());
            // 设置同步返回地址
            request.setReturnUrl(paymentConfig.getReturnUrl());
            
            // 设置请求参数
            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(appointment.getOrderNo()); // 商户订单号
            model.setTotalAmount(appointment.getTotalAmount().toString()); // 订单金额
            model.setSubject("汽车美容服务 - " + appointment.getOrderNo()); // 订单标题
            model.setProductCode("FAST_INSTANT_TRADE_PAY"); // 产品码
            model.setBody("预约服务支付"); // 订单描述
            
            request.setBizModel(model);
            
            // 调用SDK生成表单
            String form = alipayClient.pageExecute(request).getBody();
            
            logger.info("支付宝支付订单创建成功，订单号: {}", appointment.getOrderNo());
            
            return form;
            
        } catch (AlipayApiException e) {
            logger.error("创建支付宝订单失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 处理支付宝支付回调
     */
    public boolean handleAlipayNotify(Map<String, String> params) {
        try {
            // 获取订单号
            String outTradeNo = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status");
            
            logger.info("收到支付宝回调，订单号: {}, 交易状态: {}", outTradeNo, tradeStatus);
            
            // 查询订单
            Optional<Appointment> appointmentOpt = appointmentRepository.findByOrderNo(outTradeNo);
            if (!appointmentOpt.isPresent()) {
                logger.error("订单不存在: {}", outTradeNo);
                return false;
            }
            
            Appointment appointment = appointmentOpt.get();
            
            // 验证金额
            String totalAmount = params.get("total_amount");
            if (!appointment.getTotalAmount().equals(new BigDecimal(totalAmount))) {
                logger.error("金额不匹配，订单金额: {}, 回调金额: {}", 
                    appointment.getTotalAmount(), totalAmount);
                return false;
            }
            
            // 处理支付状态
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 更新支付状态
                appointment.setPaymentStatus(Appointment.PaymentStatus.PAID);
                appointment.setPaymentMethod("ALIPAY");
                appointment.setPaymentTime(LocalDateTime.now());
                appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
                appointmentRepository.save(appointment);
                
                logger.info("订单支付成功: {}", outTradeNo);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("处理支付宝回调失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 查询支付状态
     */
    public Map<String, Object> queryPaymentStatus(Long appointmentId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (!appointmentOpt.isPresent()) {
            throw new RuntimeException("预约不存在");
        }
        
        Appointment appointment = appointmentOpt.get();
        
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", appointment.getOrderNo());
        result.put("paymentStatus", appointment.getPaymentStatus());
        result.put("paymentMethod", appointment.getPaymentMethod());
        result.put("paymentTime", appointment.getPaymentTime());
        result.put("amount", appointment.getTotalAmount());
        
        return result;
    }
    
}
