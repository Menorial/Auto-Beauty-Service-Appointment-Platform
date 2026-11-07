package com.autobeauty.service;

import com.autobeauty.dto.CaptchaResponse;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图形/文本验证码（服务端存储，一次性校验）。
 * 使用内存 Map，适合单机部署；多实例需改为 Redis 等。
 */
@Service
public class CaptchaService {

    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int LENGTH = 6;
    private static final long TTL_MS = 5 * 60 * 1000L;

    private final SecureRandom random = new SecureRandom();
    private final ConcurrentHashMap<String, CaptchaEntry> store = new ConcurrentHashMap<>();

    private static class CaptchaEntry {
        final String code;
        final long expireAt;

        CaptchaEntry(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }

    public CaptchaResponse create() {
        sweepExpired();
        String id = UUID.randomUUID().toString();
        String code = randomCode();
        store.put(id, new CaptchaEntry(code, System.currentTimeMillis() + TTL_MS));
        return new CaptchaResponse(id, code);
    }

    /**
     * 校验并删除（一次性使用）。大小写敏感。
     */
    public boolean validateAndConsume(String captchaId, String userInput) {
        if (captchaId == null || captchaId.isEmpty() || userInput == null) {
            return false;
        }
        CaptchaEntry entry = store.remove(captchaId.trim());
        if (entry == null) {
            return false;
        }
        if (System.currentTimeMillis() > entry.expireAt) {
            return false;
        }
        return entry.code.equals(userInput.trim());
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }

    private void sweepExpired() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, CaptchaEntry>> it = store.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, CaptchaEntry> e = it.next();
            if (e.getValue().expireAt < now) {
                it.remove();
            }
        }
    }
}
