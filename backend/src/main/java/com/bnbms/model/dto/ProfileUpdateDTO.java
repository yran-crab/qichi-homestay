package com.bnbms.model.dto;

import lombok.Data;

import javax.validation.constraints.Pattern;

@Data
public class ProfileUpdateDTO {

    private String realName;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String email;

    private String avatar;
}
