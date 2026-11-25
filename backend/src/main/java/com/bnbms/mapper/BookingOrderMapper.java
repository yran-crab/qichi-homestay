package com.bnbms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bnbms.entity.BookingOrder;
import com.bnbms.model.vo.BookedDateVO;
import com.bnbms.model.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BookingOrderMapper extends BaseMapper<BookingOrder> {

    /**
     * 统计档期冲突订单数（待支付/待入住/已入住视为占用档期）
     * 区间重叠条件：o.check_in < :checkOut AND o.check_out > :checkIn
     */
    long countConflict(@Param("homestayId") Long homestayId,
                       @Param("checkIn") LocalDate checkIn,
                       @Param("checkOut") LocalDate checkOut,
                       @Param("excludeOrderId") Long excludeOrderId);

    IPage<OrderVO> selectTenantPage(IPage<OrderVO> page,
                                    @Param("tenantId") Long tenantId,
                                    @Param("status") String status);

    IPage<OrderVO> selectHostPage(IPage<OrderVO> page,
                                  @Param("hostId") Long hostId,
                                  @Param("status") String status);

    IPage<OrderVO> selectAdminPage(IPage<OrderVO> page,
                                   @Param("status") String status,
                                   @Param("keyword") String keyword);

    OrderVO selectVoById(@Param("id") Long id);

    /** 房源被占用的日期区间（有效状态订单） */
    List<BookedDateVO> selectBookedDates(@Param("homestayId") Long homestayId);

    /**
     * 批量取消超时未支付订单，返回影响行数
     */
    int batchCancelTimeout(@Param("minutes") int minutes, @Param("reason") String reason);
}
