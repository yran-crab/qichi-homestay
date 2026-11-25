package com.bnbms.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class RegisterDTO {

    @NotBlank(message = "请输入用户名")
    @Size(min = 4, max = 20, message = "用户名长度为 4-20 个字符")
    private String username;

    @NotBlank(message = "请输入手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "请输入昵称或姓名")
    private String realName;

    @NotBlank(message = "请设置密码")
    @Size(min = 6, max = 32, message = "密码长度为 6-32 位")
    private String password;
}
