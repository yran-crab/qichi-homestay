package com.bnbms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("homestay_facility")
public class HomestayFacility {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long homestayId;

    private Long facilityId;
}
