package com.bnbms.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class FacilitySaveDTO {

    @NotBlank(message = "设施名称不能为空")
    @Size(max = 32, message = "设施名称最长 32 字")
    private String name;

    @Size(max = 16, message = "图标最长 16 字")
    private String icon;

    private Integer sort = 0;
}
