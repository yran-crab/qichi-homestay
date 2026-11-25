package com.bnbms.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bnbms.common.BusinessException;
import com.bnbms.common.Constants;
import com.bnbms.common.PageResult;
import com.bnbms.common.RedisHelper;
import com.bnbms.config.AlipayProperties;
import com.bnbms.entity.BookingOrder;
import com.bnbms.entity.Homestay;
import com.bnbms.mapper.BookingOrderMapper;
import com.bnbms.mapper.HomestayMapper;
import com.bnbms.model.dto.BookingCreateDTO;
import com.bnbms.model.dto.OrderQueryDTO;
import com.bnbms.model.vo.BookedDateVO;
import com.bnbms.model.vo.BookingConfirmVO;
import com.bnbms.model.vo.CouponRedeemVO;
import com.bnbms.model.vo.OrderVO;
import com.bnbms.service.AlipayService;
import com.bnbms.service.BookingService;
import com.bnbms.service.CouponService;
import com.bnbms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingOrderMapper bookingOrderMapper;
    private final HomestayMapper homestayMapper;
    private final RedisHelper redisHelper;
    private final TransactionTemplate transactionTemplate;
    private final DashboardService dashboardService;
    private final AlipayService alipayService;
    private final AlipayProperties alipayProperties;
    private final CouponService couponService;

    /** 待支付订单有效期（分钟） */
    private static final int PAY_TIMEOUT_MINUTES = 30;
    /** 房源分布式锁过期时间（秒） */
    private static final long LOCK_EXPIRE_SECONDS = 30;

    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /* ============================ 预订 / 下单 ============================ */

    @Override
    public BookingConfirmVO confirm(Long tenantId, BookingCreateDTO dto) {
        Homestay h = validateBookable(dto);
        long nights = ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate());
        BigDecimal totalAmount = h.getPrice().multiply(BigDecimal.valueOf(nights));

        BookingConfirmVO vo = new BookingConfirmVO();
        vo.setHomestayId(h.getId());
        vo.setHomestayTitle(h.getTitle());
        vo.setCoverImg(h.getCoverImg());
        vo.setCity(h.getCity());
        vo.setAddress(h.getAddress());
        vo.setCheckInDate(dto.getCheckInDate());
        vo.setCheckOutDate(dto.getCheckOutDate());
        vo.setNights((int) nights);
        vo.setGuestCount(dto.getGuestCount());
        vo.setMaxGuests(h.getMaxGuests());
        vo.setUnitPrice(h.getPrice());
        vo.setTotalAmount(totalAmount);

        // 优惠券试算：服务端校验券归属/有效期/门槛并重算金额
        if (dto.getCouponId() != null) {
            CouponRedeemVO redeem = couponService.redeem(dto.getCouponId(), tenantId, totalAmount);
            vo.setCouponId(redeem.getUserCouponId());
            vo.setCouponName(redeem.getCouponName());
            vo.setDiscountAmount(redeem.getDiscountAmount());
            vo.setPayAmount(totalAmount.subtract(redeem.getDiscountAmount()));
        } else {
            vo.setDiscountAmount(BigDecimal.ZERO.setScale(2));
            vo.setPayAmount(totalAmount);
        }
        return vo;
    }

    @Override
    public OrderVO createOrder(Long tenantId, BookingCreateDTO dto) {
        Homestay h = validateBookable(dto);
        String lockKey = Constants.REDIS_LOCK_HOMESTAY + h.getId();
        String token = UUID.randomUUID().toString();
        Boolean locked = redisHelper.setIfAbsent(lockKey, token, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            throw new BusinessException("该房源正在处理其他预订，请稍后再试");
        }
        try {
            Long orderId = transactionTemplate.execute(status -> {
                // 锁内二次档期校验，防止并发穿透
                if (bookingOrderMapper.countConflict(h.getId(),
                        dto.getCheckInDate(), dto.getCheckOutDate(), null) > 0) {
                    throw new BusinessException("所选日期已被预订，请更换日期");
                }
                BigDecimal totalAmount = h.getPrice().multiply(BigDecimal.valueOf(
                        ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate())));
                // 服务端重新校验优惠券并计算抵扣（不信任前端金额）
                CouponRedeemVO redeem = null;
                if (dto.getCouponId() != null) {
                    redeem = couponService.redeem(dto.getCouponId(), tenantId, totalAmount);
                }
                BookingOrder order = buildOrder(tenantId, h, dto, totalAmount, redeem);
                bookingOrderMapper.insert(order);
                // 订单落库后原子核销优惠券（失败则整笔订单回滚）
                if (redeem != null) {
                    couponService.bindUsed(redeem.getUserCouponId(), tenantId, order.getId());
                }
                homestayMapper.update(null,
                        new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Homestay>()
                                .eq(Homestay::getId, h.getId())
                                .setSql("order_count = order_count + 1"));
                return order.getId();
            });
            return bookingOrderMapper.selectVoById(orderId);
        } finally {
            releaseLock(lockKey, token);
            evictDashboard(h.getHostId());
        }
    }

    private BookingOrder buildOrder(Long tenantId, Homestay h, BookingCreateDTO dto,
                                    BigDecimal totalAmount, CouponRedeemVO redeem) {
        BookingOrder order = new BookingOrder();
        order.setOrderNo(generateOrderNo());
        order.setTenantId(tenantId);
        order.setHostId(h.getHostId());
        order.setHomestayId(h.getId());
        order.setCheckInDate(dto.getCheckInDate());
        order.setCheckOutDate(dto.getCheckOutDate());
        order.setNights((int) ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate()));
        order.setGuestCount(dto.getGuestCount());
        order.setUnitPrice(h.getPrice());
        order.setTotalAmount(totalAmount);
        if (redeem != null) {
            order.setCouponId(redeem.getUserCouponId());
            order.setCouponName(redeem.getCouponName());
            order.setDiscountAmount(redeem.getDiscountAmount());
            order.setPayAmount(totalAmount.subtract(redeem.getDiscountAmount()));
        } else {
            order.setDiscountAmount(BigDecimal.ZERO.setScale(2));
            order.setPayAmount(totalAmount);
        }
        order.setContactName(dto.getContactName().trim());
        order.setContactPhone(dto.getContactPhone().trim());
        order.setStatus(Constants.ORDER_PENDING_PAY);
        order.setReviewed(0);
        return order;
    }

    /**
     * 订单号：BN + 14 位时间戳 + 4 位随机数；唯一键冲突时重试一次
     */
    private String generateOrderNo() {
        for (int i = 0; i < 2; i++) {
            String no = "BN" + LocalDateTime.now().format(ORDER_NO_FMT)
                    + String.format("%04d", (int) (Math.random() * 10000));
            Long exists = bookingOrderMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BookingOrder>()
                            .eq(BookingOrder::getOrderNo, no));
            if (exists == 0) {
                return no;
            }
        }
        throw new BusinessException("订单号生成冲突，请重试");
    }

    /** 下单/试算前置校验：房源状态、日期、人数、档期 */
    private Homestay validateBookable(BookingCreateDTO dto) {
        Homestay h = homestayMapper.selectById(dto.getHomestayId());
        if (h == null || !Constants.STAY_APPROVED.equals(h.getStatus())) {
            throw new BusinessException("房源不存在或已下架，无法预订");
        }
        if (dto.getCheckOutDate() == null || !dto.getCheckOutDate().isAfter(dto.getCheckInDate())) {
            throw new BusinessException("退房日期必须晚于入住日期");
        }
        if (dto.getCheckInDate().isBefore(LocalDate.now())) {
            throw new BusinessException("入住日期不能早于今天");
        }
        if (dto.getGuestCount() == null || dto.getGuestCount() < 1) {
            throw new BusinessException("入住人数不合法");
        }
        if (dto.getGuestCount() > h.getMaxGuests()) {
            throw new BusinessException("入住人数超过房源上限（" + h.getMaxGuests() + " 人）");
        }
        if (bookingOrderMapper.countConflict(h.getId(),
                dto.getCheckInDate(), dto.getCheckOutDate(), null) > 0) {
            throw new BusinessException("所选日期已被预订，请更换日期");
        }
        return h;
    }

    /* ============================ 支付 / 取消 / 履约 ============================ */

    @Override
    public String startAlipayPay(Long tenantId, Long orderId) {
        BookingOrder order = getOwnedTenantOrder(orderId, tenantId);
        if (!Constants.ORDER_PENDING_PAY.equals(order.getStatus())) {
            throw new BusinessException("仅待支付订单可以支付");
        }
        if (isExpired(order)) {
            throw new BusinessException("订单已超时未支付，请重新下单");
        }
        String subject = "民宿预订-" + order.getOrderNo();
        String returnUrl = alipayProperties.getReturnUrlBase() + "/orders/cashier/" + orderId;
        return alipayService.createPagePay(order.getOrderNo(), order.getPayAmount(), subject, returnUrl);
    }

    @Override
    public boolean verifyAlipayPay(Long tenantId, Long orderId) {
        BookingOrder order = getOwnedTenantOrder(orderId, tenantId);
        // 已支付直接返回成功
        if (Constants.ORDER_PAID.equals(order.getStatus())) {
            return true;
        }
        if (!Constants.ORDER_PENDING_PAY.equals(order.getStatus())) {
            throw new BusinessException("当前订单状态无法校验支付");
        }
        if (isExpired(order)) {
            throw new BusinessException("订单已超时未支付，请重新下单");
        }
        boolean paid = alipayService.isTradeSuccess(order.getOrderNo());
        if (paid) {
            markPaid(order);
        }
        return paid;
    }

    /** 标记订单已支付 */
    private void markPaid(BookingOrder order) {
        order.setStatus(Constants.ORDER_PAID);
        order.setPayTime(LocalDateTime.now());
        bookingOrderMapper.updateById(order);
        evictDashboard(order.getHostId());
    }

    @Override
    public void cancel(Long tenantId, Long orderId, String reason) {
        BookingOrder order = getOwnedTenantOrder(orderId, tenantId);
        if (Constants.ORDER_PENDING_PAY.equals(order.getStatus())) {
            // 待支付取消：退回优惠券
            doCancel(order, reason, true);
            return;
        }
        if (Constants.ORDER_PAID.equals(order.getStatus())) {
            // 待入住订单距入住日至少 1 天（入住日晚于今天）方可取消
            if (!order.getCheckInDate().isAfter(LocalDate.now())) {
                throw new BusinessException("入住日当天或已过入住日，无法线上取消，请联系房东");
            }
            // 已支付订单取消：优惠券已核销，不退回
            doCancel(order, reason, false);
            return;
        }
        throw new BusinessException("当前订单状态不可取消");
    }

    @Override
    public void checkIn(Long hostId, Long orderId) {
        BookingOrder order = getOwnedHostOrder(orderId, hostId);
        if (!Constants.ORDER_PAID.equals(order.getStatus())) {
            throw new BusinessException("仅待入住订单可以确认入住");
        }
        order.setStatus(Constants.ORDER_CHECKED_IN);
        bookingOrderMapper.updateById(order);
        evictDashboard(hostId);
    }

    @Override
    public void checkOut(Long hostId, Long orderId) {
        BookingOrder order = getOwnedHostOrder(orderId, hostId);
        if (!Constants.ORDER_CHECKED_IN.equals(order.getStatus())) {
            throw new BusinessException("仅已入住订单可以办理退房");
        }
        order.setStatus(Constants.ORDER_COMPLETED);
        bookingOrderMapper.updateById(order);
        evictDashboard(hostId);
    }

    private void doCancel(BookingOrder order, String reason, boolean refundCoupon) {
        transactionTemplate.executeWithoutResult(status -> {
            order.setStatus(Constants.ORDER_CANCELLED);
            order.setCancelTime(LocalDateTime.now());
            order.setCancelReason(reason != null && !reason.trim().isEmpty()
                    ? reason.trim() : "租客主动取消");
            bookingOrderMapper.updateById(order);
            homestayMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Homestay>()
                            .eq(Homestay::getId, order.getHomestayId())
                            .setSql("order_count = GREATEST(order_count - 1, 0)"));
            // 仅待支付取消时退回优惠券
            if (refundCoupon && order.getCouponId() != null) {
                couponService.refundByOrder(order.getId());
            }
        });
        evictDashboard(order.getHostId());
    }

    private boolean isExpired(BookingOrder order) {
        return order.getCreatedAt() != null
                && order.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(PAY_TIMEOUT_MINUTES));
    }

    /* ============================ 查询 ============================ */

    @Override
    public PageResult<OrderVO> tenantPage(Long tenantId, OrderQueryDTO query) {
        Page<OrderVO> page = new Page<>(query.getPage(), query.getPageSize());
        bookingOrderMapper.selectTenantPage(page, tenantId, emptyAsNull(query.getStatus()));
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    @Override
    public PageResult<OrderVO> hostPage(Long hostId, OrderQueryDTO query) {
        Page<OrderVO> page = new Page<>(query.getPage(), query.getPageSize());
        bookingOrderMapper.selectHostPage(page, hostId, emptyAsNull(query.getStatus()));
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    @Override
    public PageResult<OrderVO> adminPage(OrderQueryDTO query) {
        Page<OrderVO> page = new Page<>(query.getPage(), query.getPageSize());
        String keyword = query.getKeyword() != null && !query.getKeyword().trim().isEmpty()
                ? query.getKeyword().trim() : null;
        bookingOrderMapper.selectAdminPage(page, emptyAsNull(query.getStatus()), keyword);
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    @Override
    public OrderVO getDetail(Long userId, String role, Long orderId) {
        OrderVO vo = bookingOrderMapper.selectVoById(orderId);
        if (vo == null) {
            throw new BusinessException("订单不存在");
        }
        if (Constants.ROLE_ADMIN.equals(role)) {
            return vo;
        }
        if (Constants.ROLE_HOST.equals(role) && vo.getHostId().equals(userId)) {
            return vo;
        }
        if (vo.getTenantId().equals(userId)) {
            return vo;
        }
        throw new BusinessException(com.bnbms.common.Result.CODE_FORBIDDEN, "无权查看该订单");
    }

    @Override
    public List<BookedDateVO> bookedDates(Long homestayId) {
        return bookingOrderMapper.selectBookedDates(homestayId);
    }

    /* ============================ 定时任务 ============================ */

    /**
     * 每分钟扫描，取消超过 30 分钟未支付的订单，并回滚房源订单计数
     */
    @Override
    @Scheduled(cron = "30 * * * * ?")
    public int cancelTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(PAY_TIMEOUT_MINUTES);
        List<BookingOrder> timeoutOrders = bookingOrderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BookingOrder>()
                        .eq(BookingOrder::getStatus, Constants.ORDER_PENDING_PAY)
                        .lt(BookingOrder::getCreatedAt, deadline)
                        .last("LIMIT 200"));
        if (timeoutOrders.isEmpty()) {
            return 0;
        }
        String reason = "超时 " + PAY_TIMEOUT_MINUTES + " 分钟未支付，系统自动取消";
        int rows = bookingOrderMapper.batchCancelTimeout(PAY_TIMEOUT_MINUTES, reason);
        transactionTemplate.executeWithoutResult(status -> {
            for (BookingOrder o : timeoutOrders) {
                homestayMapper.update(null,
                        new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Homestay>()
                                .eq(Homestay::getId, o.getHomestayId())
                                .setSql("order_count = GREATEST(order_count - 1, 0)"));
                // 超时未支付自动取消：退回优惠券
                if (o.getCouponId() != null) {
                    couponService.refundByOrder(o.getId());
                }
            }
        });
        log.info("定时取消超时未支付订单 {} 条", rows);
        return rows;
    }

    /* ============================ 私有方法 ============================ */

    private BookingOrder getOwnedTenantOrder(Long orderId, Long tenantId) {
        BookingOrder order = bookingOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getTenantId().equals(tenantId)) {
            throw new BusinessException(com.bnbms.common.Result.CODE_FORBIDDEN, "无权操作该订单");
        }
        return order;
    }

    private BookingOrder getOwnedHostOrder(Long orderId, Long hostId) {
        BookingOrder order = bookingOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getHostId().equals(hostId)) {
            throw new BusinessException(com.bnbms.common.Result.CODE_FORBIDDEN, "无权操作该订单");
        }
        return order;
    }

    private void releaseLock(String key, String token) {
        Object value = redisHelper.get(key);
        if (token.equals(value)) {
            redisHelper.delete(key);
        }
    }

    /** 订单状态变更后清理看板缓存（管理员 + 房东） */
    private void evictDashboard(Long hostId) {
        try {
            dashboardService.evictAdminCache();
            if (hostId != null) {
                dashboardService.evictHostCache(hostId);
            }
        } catch (Exception e) {
            log.warn("清理看板缓存失败: {}", e.getMessage());
        }
    }

    private String emptyAsNull(String s) {
        return s != null && !s.trim().isEmpty() ? s.trim() : null;
    }
}
