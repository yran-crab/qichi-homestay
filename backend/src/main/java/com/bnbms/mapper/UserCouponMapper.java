package com.bnbms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bnbms.entity.UserCoupon;
import com.bnbms.model.vo.UserCouponVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    /**
     * 原子核销：仅当券属于该用户且状态为 UNUSED 时才能改为 USED。
     * 返回影响行数 0 表示券不存在/不属于本人/已被使用。
     */
    int markUsed(@Param("id") Long id,
                 @Param("userId") Long userId,
                 @Param("orderId") Long orderId);

    /**
     * 原子退券：仅当券已绑定该订单且状态为 USED 时才能回退为 UNUSED。
     */
    int markUnused(@Param("orderId") Long orderId);

    /**
     * 查询用户全部优惠券（关联模板规则），过期状态在 Service 层动态计算
     */
    List<UserCouponVO> selectMyList(@Param("userId") Long userId);
}
