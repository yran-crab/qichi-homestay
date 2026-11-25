package com.bnbms.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long homestayId;
    private Long tenantId;
    private Integer rating;
    private String content;
    /** 评价图片，逗号分隔 URL */
    private String images;
    private String reply;
    private LocalDateTime replyAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
