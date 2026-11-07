package com.autobeauty.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付配置类
 */
@Configuration
public class PaymentConfig {
    
    // 支付宝配置
    @Value("${payment.alipay.app-id}")
    private String alipayAppId;
    
    @Value("${payment.alipay.merchant-private-key}")
    private String merchantPrivateKey;
    
    @Value("${payment.alipay.alipay-public-key}")
    private String alipayPublicKey;
    
    @Value("${payment.alipay.gateway-url}")
    private String gatewayUrl;
    
    @Value("${payment.alipay.format}")
    private String format;
    
    @Value("${payment.alipay.charset}")
    private String charset;
    
    @Value("${payment.alipay.sign-type}")
    private String signType;
    
    @Value("${payment.alipay.notify-url}")
    private String notifyUrl;
    
    @Value("${payment.alipay.return-url}")
    private String returnUrl;
    
    /**
     * 初始化支付宝客户端
     */
    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(
            gatewayUrl,
            alipayAppId,
            merchantPrivateKey,
            format,
            charset,
            alipayPublicKey,
            signType
        );
    }
    
    // Getters
    public String getAlipayAppId() {
        return alipayAppId;
    }
    
    public String getMerchantPrivateKey() {
        return merchantPrivateKey;
    }
    
    public String getAlipayPublicKey() {
        return alipayPublicKey;
    }
    
    public String getGatewayUrl() {
        return gatewayUrl;
    }
    
    public String getFormat() {
        return format;
    }
    
    public String getCharset() {
        return charset;
    }
    
    public String getSignType() {
        return signType;
    }
    
    public String getNotifyUrl() {
        return notifyUrl;
    }
    
    public String getReturnUrl() {
        return returnUrl;
    }
}
