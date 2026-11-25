package com.bnbms.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class BannerSaveDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题不能超过 100 字")
    private String title;

    @NotBlank(message = "图片 URL 不能为空")
    @Size(max = 500, message = "图片 URL 不能超过 500 字")
    private String imageUrl;

    @Size(max = 500, message = "跳转链接不能超过 500 字")
    private String link;

    @NotNull(message = "排序不能为空")
    private Integer sort;

    private Integer status;
}
