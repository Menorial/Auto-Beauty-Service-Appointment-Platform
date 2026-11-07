package com.autobeauty.util;

import com.autobeauty.entity.Vehicle;
import com.autobeauty.entity.Vehicle.VehicleType;
import java.util.regex.Pattern;

/**
 * 车牌号验证工具类
 * 支持燃油车、普通混动车、插电式混动车和纯电动车的车牌号格式验证
 */
public class LicensePlateValidator {
    
    // 传统车牌（燃油车、普通混动车）：省份简称 + 字母 + 5位数字或字母 (如：京A12345)
    // 注意：序号中不使用字母"O"和"I"避免混淆
    private static final String TRADITIONAL_PLATE_PATTERN = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][0-9A-HJ-NP-Z]{5}$";
    
    // 新能源车牌（插电式混动车、纯电动车）：
    // 小型车：省份简称 + 字母 + D/F + 5位数字或字母 (如：京AD12345, 京AF12345)
    // 大型车：省份简称 + 字母 + 5位数字或字母 + D/F (如：京A12345D, 京A12345F)
    private static final String NEW_ENERGY_SMALL_D_PATTERN = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z]D[0-9A-HJ-NP-Z]{5}$";
    private static final String NEW_ENERGY_SMALL_F_PATTERN = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z]F[0-9A-HJ-NP-Z]{5}$";
    private static final String NEW_ENERGY_LARGE_D_PATTERN = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][0-9A-HJ-NP-Z]{5}D$";
    private static final String NEW_ENERGY_LARGE_F_PATTERN = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][0-9A-HJ-NP-Z]{5}F$";
    
    private static final Pattern TRADITIONAL_PATTERN = Pattern.compile(TRADITIONAL_PLATE_PATTERN);
    private static final Pattern NEW_ENERGY_SMALL_D_COMPILED = Pattern.compile(NEW_ENERGY_SMALL_D_PATTERN);
    private static final Pattern NEW_ENERGY_SMALL_F_COMPILED = Pattern.compile(NEW_ENERGY_SMALL_F_PATTERN);
    private static final Pattern NEW_ENERGY_LARGE_D_COMPILED = Pattern.compile(NEW_ENERGY_LARGE_D_PATTERN);
    private static final Pattern NEW_ENERGY_LARGE_F_COMPILED = Pattern.compile(NEW_ENERGY_LARGE_F_PATTERN);
    
    /**
     * 验证车牌号是否符合指定车辆类型的格式
     * @param licensePlate 车牌号
     * @param vehicleType 车辆类型
     * @return 是否有效
     */
    public static boolean isValid(String licensePlate, VehicleType vehicleType) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            return false;
        }
        
        String upperLicensePlate = licensePlate.toUpperCase().trim();
        
        switch (vehicleType) {
            case FUEL:
            case HEV:  // 普通混动车使用传统燃油车车牌格式
                return TRADITIONAL_PATTERN.matcher(upperLicensePlate).matches();
            case PHEV:  // 插电式混动车使用新能源车牌，F标识
                return NEW_ENERGY_SMALL_F_COMPILED.matcher(upperLicensePlate).matches() ||
                       NEW_ENERGY_LARGE_F_COMPILED.matcher(upperLicensePlate).matches();
            case BEV:   // 纯电动车使用新能源车牌，D标识
                return NEW_ENERGY_SMALL_D_COMPILED.matcher(upperLicensePlate).matches() ||
                       NEW_ENERGY_LARGE_D_COMPILED.matcher(upperLicensePlate).matches();
            default:
                return false;
        }
    }
    
    /**
     * 自动检测车牌号类型
     * @param licensePlate 车牌号
     * @return 车辆类型，如果无法识别返回null
     */
    public static VehicleType detectVehicleType(String licensePlate) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            return null;
        }
        
        String upperLicensePlate = licensePlate.toUpperCase().trim();
        
        // 检查是否为纯电动车牌（D标识）
        if (NEW_ENERGY_SMALL_D_COMPILED.matcher(upperLicensePlate).matches() ||
            NEW_ENERGY_LARGE_D_COMPILED.matcher(upperLicensePlate).matches()) {
            return VehicleType.BEV;
        }
        
        // 检查是否为插电式混动车牌（F标识）
        if (NEW_ENERGY_SMALL_F_COMPILED.matcher(upperLicensePlate).matches() ||
            NEW_ENERGY_LARGE_F_COMPILED.matcher(upperLicensePlate).matches()) {
            return VehicleType.PHEV;
        }
        
        // 检查是否为传统车牌（燃油车或普通混动车）
        if (TRADITIONAL_PATTERN.matcher(upperLicensePlate).matches()) {
            return VehicleType.FUEL; // 默认返回燃油车，无法区分燃油车和普通混动车
        }
        
        return null;
    }
    
    /**
     * 获取车辆类型的车牌格式描述
     * @param vehicleType 车辆类型
     * @return 格式描述
     */
    public static String getFormatDescription(VehicleType vehicleType) {
        switch (vehicleType) {
            case FUEL:
                return "燃油车车牌格式：省份简称+字母+5位数字或字母，如：京A12345";
            case HEV:
                return "普通混动车车牌格式：省份简称+字母+5位数字或字母，如：京A12345（使用传统燃油车车牌）";
            case PHEV:
                return "插电式混动车车牌格式：小型车为省份简称+字母+F+5位数字或字母（如：京AF12345），大型车为省份简称+字母+5位数字或字母+F（如：京A12345F）";
            case BEV:
                return "纯电动车车牌格式：小型车为省份简称+字母+D+5位数字或字母（如：京AD12345），大型车为省份简称+字母+5位数字或字母+D（如：京A12345D）";
            default:
                return "未知车辆类型";
        }
    }
    
    /**
     * 验证车牌号并返回详细错误信息
     * @param licensePlate 车牌号
     * @param vehicleType 车辆类型
     * @return 验证结果，null表示验证通过，否则返回错误信息
     */
    public static String validateWithMessage(String licensePlate, VehicleType vehicleType) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            return "车牌号不能为空";
        }
        
        if (isValid(licensePlate, vehicleType)) {
            return null; // 验证通过
        }
        
        return getFormatDescription(vehicleType);
    }
}
