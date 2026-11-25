package com.bnbms.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginDTO {

    @NotBlank(message = "请输入用户名或手机号")
    private String username;

    @NotBlank(message = "请输入密码")
    private String password;

    /** 图形验证码（Redis 不可用时可选，后端自动跳过校验） */
    private String captcha;

    /** 验证码标识（Redis 不可用时可选） */
    private String captchaKey;
}
