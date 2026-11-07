package com.autobeauty.util;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageUtils {
    
    private static final Pattern DATA_URL_PATTERN = Pattern.compile("^data:image/[^;]+;base64,(.+)$");
    
    /**
     * 从Data URL中提取Base64数据并解码为字节数组
     * @param dataUrl 格式：data:image/jpeg;base64,/9j/4AAQ...
     * @return 图片的字节数组
     */
    public static byte[] decodeDataUrl(String dataUrl) {
        if (dataUrl == null || dataUrl.trim().isEmpty()) {
            return null;
        }
        
        // 如果不是data URL格式，返回null
        if (!dataUrl.startsWith("data:")) {
            return null;
        }
        
        Matcher matcher = DATA_URL_PATTERN.matcher(dataUrl);
        if (matcher.matches()) {
            String base64Data = matcher.group(1);
            try {
                return Base64.getDecoder().decode(base64Data);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * 检查是否为Data URL格式
     */
    public static boolean isDataUrl(String url) {
        return url != null && url.startsWith("data:image/");
    }
    
    /**
     * 将字节数组编码为Data URL
     * @param imageData 图片字节数组
     * @param mimeType MIME类型，如 "image/jpeg"
     * @return Data URL格式的字符串
     */
    public static String encodeToDataUrl(byte[] imageData, String mimeType) {
        if (imageData == null || imageData.length == 0) {
            return null;
        }
        
        String base64 = Base64.getEncoder().encodeToString(imageData);
        return "data:" + (mimeType != null ? mimeType : "image/jpeg") + ";base64," + base64;
    }
}
