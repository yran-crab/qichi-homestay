package com.bnbms.model.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * 房东发布/编辑房源
 */
@Data
public class HomestaySaveDTO {

    @NotBlank(message = "房源标题不能为空")
    @Size(max = 100, message = "标题最长 100 字")
    private String title;

    @NotBlank(message = "所在城市不能为空")
    private String city;

    @NotBlank(message = "详细地址不能为空")
    private String address;

    private String coverImg;

    /** 图片 URL 列表（最多 10 张） */
    private List<String> images;

    @NotBlank(message = "房源类型不能为空")
    private String type;

    @NotNull(message = "每晚价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    private BigDecimal price;

    private BigDecimal originalPrice;

    private BigDecimal area;

    @NotNull
    @Min(value = 0, message = "卧室数不合法")
    private Integer bedrooms;

    @NotNull
    @Min(value = 0, message = "客厅数不合法")
    private Integer livingRooms;

    @NotNull
    @Min(value = 1, message = "至少 1 个卫生间")
    private Integer bathrooms;

    @NotNull
    @Min(value = 1, message = "至少 1 张床")
    private Integer beds;

    @NotNull
    @Min(value = 1, message = "至少可住 1 人")
    private Integer maxGuests;

    private String floorInfo;

    @NotBlank(message = "房源描述不能为空")
    private String description;

    private String rules;

    /** 关联设施 ID */
    @NotEmpty(message = "请至少选择一项配套设施")
    private List<Long> facilityIds;
}
