package com.bnbms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bnbms.entity.Favorite;
import com.bnbms.model.vo.HomestayCardVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    /**
     * 我的收藏分页（关联房源卡片信息，含房源状态用于下架标识），按收藏时间倒序
     */
    IPage<HomestayCardVO> selectMyFavoritePage(IPage<HomestayCardVO> page, @Param("userId") Long userId);
}
