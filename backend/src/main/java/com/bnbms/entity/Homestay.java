package com.bnbms.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("homestay")
public class Homestay {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long hostId;

    private String title;

    private String city;

    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String coverImg;

    /** 图片 URL 逗号分隔 */
    private String images;

    /** 整租/单间/公寓/别墅/小院 */
    private String type;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private BigDecimal area;

    private Integer bedrooms;

    private Integer livingRooms;

    private Integer bathrooms;

    private Integer beds;

    private Integer maxGuests;

    private String floorInfo;

    private String description;

    private String rules;

    /** PENDING/APPROVED/REJECTED/OFFLINE */
    private String status;

    private String auditRemark;

    private Integer viewCount;

    private Integer favoriteCount;

    private Integer orderCount;

    private BigDecimal rating;

    private Integer reviewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
