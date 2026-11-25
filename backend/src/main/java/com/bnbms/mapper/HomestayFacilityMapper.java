package com.bnbms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bnbms.entity.HomestayFacility;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HomestayFacilityMapper extends BaseMapper<HomestayFacility> {

    /**
     * 批量查询多个房源的设施关联（一次查询避免 N+1）
     */
    List<HomestayFacility> selectByHomestayIds(@Param("ids") List<Long> ids);
}
