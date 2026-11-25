package com.bnbms.service;

import com.bnbms.common.PageResult;
import com.bnbms.model.dto.BookingCreateDTO;
import com.bnbms.model.dto.OrderQueryDTO;
import com.bnbms.model.vo.BookedDateVO;
import com.bnbms.model.vo.BookingConfirmVO;
import com.bnbms.model.vo.OrderVO;

import java.util.List;

public interface BookingService {

    /** 预订试算：校验日期/人数/档期/优惠券并返回金额明细 */
    BookingConfirmVO confirm(Long tenantId, BookingCreateDTO dto);

    /** 创建订单（Redis 房源锁 + 锁内二次档期校验） */
    OrderVO createOrder(Long tenantId, BookingCreateDTO dto);

    /** 发起支付宝沙箱支付，返回自动提交的支付表单 HTML */
    String startAlipayPay(Long tenantId, Long orderId);

    /** 支付宝回跳后查询交易状态并标记订单已支付，返回是否支付成功 */
    boolean verifyAlipayPay(Long tenantId, Long orderId);

    /** 租客取消订单 */
    void cancel(Long tenantId, Long orderId, String reason);

    /** 房东确认入住 */
    void checkIn(Long hostId, Long orderId);

    /** 房东办理退房 */
    void checkOut(Long hostId, Long orderId);

    PageResult<OrderVO> tenantPage(Long tenantId, OrderQueryDTO query);

    PageResult<OrderVO> hostPage(Long hostId, OrderQueryDTO query);

    PageResult<OrderVO> adminPage(OrderQueryDTO query);

    /** 订单详情（按角色隔离） */
    OrderVO getDetail(Long userId, String role, Long orderId);

    /** 房源已占用日期区间 */
    List<BookedDateVO> bookedDates(Long homestayId);

    /** 定时取消超时未支付订单，返回取消条数 */
    int cancelTimeoutOrders();
}
