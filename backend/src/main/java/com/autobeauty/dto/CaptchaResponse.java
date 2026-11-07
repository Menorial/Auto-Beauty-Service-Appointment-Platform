package com.autobeauty.dto;

/**
 * 获取验证码接口返回：展示用字符串 + 提交时带回的 captchaId。
 */
public class CaptchaResponse {

    private String captchaId;
    /** 展示给用户看的验证码（与 session 方案不同，由服务端生成并返回） */
    private String captcha;

    public CaptchaResponse() {
    }

    public CaptchaResponse(String captchaId, String captcha) {
        this.captchaId = captchaId;
        this.captcha = captcha;
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }
}
