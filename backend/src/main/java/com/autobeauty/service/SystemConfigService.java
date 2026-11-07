package com.autobeauty.service;

import com.autobeauty.entity.SystemConfig;
import com.autobeauty.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SystemConfigService {
    
    @Autowired
    private SystemConfigRepository systemConfigRepository;
    
    /**
     * 获取所有系统配置
     */
    public List<SystemConfig> getAllConfigs() {
        return systemConfigRepository.findAll();
    }
    
    /**
     * 根据配置键获取配置
     */
    public Optional<SystemConfig> getConfigByKey(String configKey) {
        return systemConfigRepository.findByConfigKey(configKey);
    }
    
    /**
     * 获取配置值
     */
    public String getConfigValue(String configKey, String defaultValue) {
        return systemConfigRepository.findByConfigKey(configKey)
                .map(SystemConfig::getConfigValue)
                .orElse(defaultValue);
    }
    
    /**
     * 更新或创建配置
     */
    @Transactional
    public SystemConfig saveOrUpdateConfig(String configKey, String configValue) {
        Optional<SystemConfig> existingConfig = systemConfigRepository.findByConfigKey(configKey);
        
        if (existingConfig.isPresent()) {
            // 更新现有配置
            SystemConfig config = existingConfig.get();
            config.setConfigValue(configValue);
            return systemConfigRepository.save(config);
        } else {
            // 创建新配置
            SystemConfig newConfig = new SystemConfig();
            newConfig.setConfigKey(configKey);
            newConfig.setConfigValue(configValue);
            return systemConfigRepository.save(newConfig);
        }
    }
    
    /**
     * 批量保存或更新配置
     */
    @Transactional
    public void saveOrUpdateConfigs(List<SystemConfig> configs) {
        for (SystemConfig config : configs) {
            saveOrUpdateConfig(config.getConfigKey(), config.getConfigValue());
        }
    }
    
    /**
     * 删除配置
     */
    @Transactional
    public void deleteConfig(String configKey) {
        systemConfigRepository.findByConfigKey(configKey)
                .ifPresent(systemConfigRepository::delete);
    }
    
    /**
     * 初始化默认配置
     */
    @Transactional
    public void initDefaultConfigs() {
        // 基本设置
        saveIfNotExists("business_hours_start", "09:00", "营业开始时间");
        saveIfNotExists("business_hours_end", "18:00", "营业结束时间");
        saveIfNotExists("appointment_advance_days", "7", "可预约提前天数");
        saveIfNotExists("max_daily_appointments", "20", "每日最大预约数");
        saveIfNotExists("min_appointment_interval", "30", "预约最小间隔（分钟）");
        
        // 积分设置
        saveIfNotExists("points_per_yuan", "1", "消费积分比例（积分/元）");
        saveIfNotExists("review_points_reward", "10", "评价奖励积分");
    }
    
    /**
     * 如果不存在则保存配置
     */
    private void saveIfNotExists(String configKey, String configValue, String description) {
        if (!systemConfigRepository.existsByConfigKey(configKey)) {
            SystemConfig config = new SystemConfig(configKey, configValue, description);
            systemConfigRepository.save(config);
        }
    }
}
