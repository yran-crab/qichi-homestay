package com.bnbms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bnbms.entity.Review;
import com.bnbms.model.dto.ReviewQueryDTO;
import com.bnbms.model.vo.ReviewVO;
import org.apache.ibatis.annotations.Param;

public interface ReviewMapper extends BaseMapper<Review> {

    /** 房源详情页公开评价分页 */
    IPage<ReviewVO> selectHomestayPage(IPage<ReviewVO> page, @Param("homestayId") Long homestayId);

    /** 租客我的评价 */
    IPage<ReviewVO> selectMyPage(IPage<ReviewVO> page, @Param("tenantId") Long tenantId);

    /** 房东收到的评价（本人房源） */
    IPage<ReviewVO> selectHostPage(IPage<ReviewVO> page, @Param("hostId") Long hostId,
                                   @Param("query") ReviewQueryDTO query);

    /** 管理员全量评价 */
    IPage<ReviewVO> selectAdminPage(IPage<ReviewVO> page, @Param("query") ReviewQueryDTO query);
}
