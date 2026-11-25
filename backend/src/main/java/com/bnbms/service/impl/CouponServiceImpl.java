package com.bnbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bnbms.common.BusinessException;
import com.bnbms.common.Constants;
import com.bnbms.common.PageResult;
import com.bnbms.entity.Coupon;
import com.bnbms.entity.Homestay;
import com.bnbms.entity.UserCoupon;
import com.bnbms.mapper.CouponMapper;
import com.bnbms.mapper.HomestayMapper;
import com.bnbms.mapper.UserCouponMapper;
import com.bnbms.model.dto.CouponQueryDTO;
import com.bnbms.model.dto.CouponSaveDTO;
import com.bnbms.model.vo.CouponCenterVO;
import com.bnbms.model.vo.CouponRedeemVO;
import com.bnbms.model.vo.UserCouponVO;
import com.bnbms.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 优惠券服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final HomestayMapper homestayMapper;

    /** 支付宝最小支付金额，优惠后订单至少支付 0.01 元 */
    private static final BigDecimal MIN_PAY = new BigDecimal("0.01");

    /* ============================ 管理端 ============================ */

    @Override
    public PageResult<Coupon> adminPage(CouponQueryDTO query) {
        Page<Coupon> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        if (query.getName() != null && !query.getName().trim().isEmpty()) {
            wrapper.like(Coupon::getName, query.getName().trim());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Coupon::getStatus, query.getStatus());
        }
        if (query.getType() != null && !query.getType().trim().isEmpty()) {
            wrapper.eq(Coupon::getType, query.getType().trim());
        }
        wrapper.orderByDesc(Coupon::getCreatedAt);
        couponMapper.selectPage(page, wrapper);
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminCreate(CouponSaveDTO dto) {
        validateSave(dto);
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(dto, coupon);
        coupon.setId(null);
        coupon.setReceivedCount(0);
        coupon.setUsedCount(0);
        coupon.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        couponMapper.insert(coupon);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdate(CouponSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("优惠券 ID 缺失");
        }
        Coupon exists = couponMapper.selectById(dto.getId());
        if (exists == null) {
            throw new BusinessException("优惠券不存在");
        }
        validateSave(dto);
        // 已被领取后不允许修改规则，避免影响存量用户券
        if (exists.getReceivedCount() != null && exists.getReceivedCount() > 0) {
            throw new BusinessException("该券已被领取，规则不可修改，可下架后重新创建");
        }
        BeanUtils.copyProperties(dto, exists);
        couponMapper.updateById(exists);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值不合法");
        }
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        couponMapper.update(null, new LambdaUpdateWrapper<Coupon>()
                .eq(Coupon::getId, id)
                .set(Coupon::getStatus, status));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDelete(Long id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        if (coupon.getReceivedCount() != null && coupon.getReceivedCount() > 0) {
            throw new BusinessException("已有用户领取该券，无法删除，建议下架处理");
        }
        couponMapper.deleteById(id);
    }

    /** 保存参数校验：类型相关字段与有效期 */
    private void validateSave(CouponSaveDTO dto) {
        String type = dto.getType();
        if (Constants.COUPON_DISCOUNT.equals(type)) {
            if (dto.getDiscountPercent() == null
                    || dto.getDiscountPercent() < 1 || dto.getDiscountPercent() > 99) {
                throw new BusinessException("折扣券需填写 1-99 之间的折扣率（88 表示 8.8 折）");
            }
            if (dto.getMaxDiscountAmount() != null && dto.getMaxDiscountAmount().signum() < 0) {
                throw new BusinessException("封顶优惠金额不能为负");
            }
        } else if (Constants.COUPON_CASH.equals(type) || Constants.COUPON_FULL_REDUCE.equals(type)) {
            if (dto.getDiscountAmount() == null || dto.getDiscountAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("请填写大于 0 的优惠金额");
            }
            if (Constants.COUPON_FULL_REDUCE.equals(type)
                    && (dto.getThreshold() == null || dto.getThreshold().compareTo(dto.getDiscountAmount()) <= 0)) {
                throw new BusinessException("满减券的使用门槛需大于优惠金额");
            }
        } else {
            throw new BusinessException("优惠券类型不合法");
        }
        if (Constants.COUPON_VALID_FIXED.equals(dto.getValidType())) {
            if (dto.getValidStartTime() == null || dto.getValidEndTime() == null) {
                throw new BusinessException("固定有效期请选择起止时间");
            }
            if (!dto.getValidEndTime().isAfter(dto.getValidStartTime())) {
                throw new BusinessException("有效期结束时间需晚于开始时间");
            }
        } else if (Constants.COUPON_VALID_DAYS.equals(dto.getValidType())) {
            if (dto.getValidDays() == null || dto.getValidDays() < 1) {
                throw new BusinessException("领取后有效天数至少 1 天");
            }
        } else {
            throw new BusinessException("有效期类型不合法");
        }
    }

    /* ============================ 租客：领券中心 / 领取 ============================ */

    @Override
    public List<CouponCenterVO> center(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> coupons = couponMapper.selectList(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getStatus, 1)
                .orderByDesc(Coupon::getCreatedAt));

        // 当前用户已领取的 couponId 集合
        List<Long> receivedCouponIds = userCouponMapper.selectList(
                        new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getUserId, userId))
                .stream().map(UserCoupon::getCouponId).collect(Collectors.toList());

        List<CouponCenterVO> result = new ArrayList<>();
        for (Coupon c : coupons) {
            // 固定有效期：不在领取窗口内不展示；领后 N 天有效：上架即可领
            if (Constants.COUPON_VALID_FIXED.equals(c.getValidType())
                    && (c.getValidStartTime() == null || c.getValidEndTime() == null
                        || now.isBefore(c.getValidStartTime()) || now.isAfter(c.getValidEndTime()))) {
                continue;
            }
            CouponCenterVO vo = new CouponCenterVO();
            BeanUtils.copyProperties(c, vo);
            vo.setReceived(receivedCouponIds.contains(c.getId()));
            boolean soldOut = c.getTotalCount() != null && c.getTotalCount() >= 0
                    && c.getReceivedCount() >= c.getTotalCount();
            vo.setSoldOut(soldOut);
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receive(Long userId, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getStatus() == null || coupon.getStatus() != 1) {
            throw new BusinessException("优惠券不存在或已下架");
        }
        LocalDateTime now = LocalDateTime.now();
        if (Constants.COUPON_VALID_FIXED.equals(coupon.getValidType())) {
            if (now.isBefore(coupon.getValidStartTime()) || now.isAfter(coupon.getValidEndTime())) {
                throw new BusinessException("该优惠券已过领取时间");
            }
        }

        // 每人限领校验
        Long owned = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId));
        if (owned >= coupon.getPerLimit()) {
            throw new BusinessException("您已领取过该优惠券（每人限领 " + coupon.getPerLimit() + " 张）");
        }

        // 原子扣减库存（不限量 totalCount = -1）
        int rows = couponMapper.update(null, new LambdaUpdateWrapper<Coupon>()
                .eq(Coupon::getId, couponId)
                .apply("(total_count = -1 OR received_count < total_count)")
                .setSql("received_count = received_count + 1"));
        if (rows == 0) {
            throw new BusinessException("该优惠券已被领完");
        }

        // 按规则生成本券有效期快照
        LocalDateTime start;
        LocalDateTime end;
        if (Constants.COUPON_VALID_DAYS.equals(coupon.getValidType())) {
            start = now;
            end = now.plusDays(coupon.getValidDays());
        } else {
            start = coupon.getValidStartTime();
            end = coupon.getValidEndTime();
        }

        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setStatus(Constants.COUPON_UNUSED);
        uc.setReceiveTime(now);
        uc.setValidStartTime(start);
        uc.setValidEndTime(end);
        userCouponMapper.insert(uc);
    }

    /* ============================ 租客：我的券 / 可用券 ============================ */

    @Override
    public List<UserCouponVO> mine(Long userId, String status) {
        List<UserCouponVO> all = userCouponMapper.selectMyList(userId);
        LocalDateTime now = LocalDateTime.now();
        return all.stream()
                .peek(vo -> vo.setStatus(displayStatus(vo, now)))
                .filter(vo -> status == null || status.trim().isEmpty() || status.equals(vo.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserCouponVO> usable(Long userId, Long homestayId, LocalDate checkIn, LocalDate checkOut) {
        Homestay h = homestayMapper.selectById(homestayId);
        if (h == null || !Constants.STAY_APPROVED.equals(h.getStatus())) {
            throw new BusinessException("房源不存在或已下架");
        }
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new BusinessException("入住日期不合法");
        }
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal total = h.getPrice().multiply(BigDecimal.valueOf(nights));

        LocalDateTime now = LocalDateTime.now();
        List<UserCouponVO> list = userCouponMapper.selectMyList(userId).stream()
                .filter(vo -> Constants.COUPON_UNUSED.equals(displayStatus(vo, now)))
                .collect(Collectors.toList());

        for (UserCouponVO vo : list) {
            boolean thresholdOk = total.compareTo(vo.getThreshold()) >= 0;
            vo.setUsable(thresholdOk);
            if (thresholdOk) {
                Coupon coupon = new Coupon();
                coupon.setType(vo.getType());
                coupon.setThreshold(vo.getThreshold());
                coupon.setDiscountAmount(vo.getDiscountAmount());
                coupon.setDiscountPercent(vo.getDiscountPercent());
                coupon.setMaxDiscountAmount(vo.getMaxDiscountAmount());
                BigDecimal discount = calcDiscount(coupon, total);
                vo.setCurrentDiscount(discount);
                vo.setCurrentPayAmount(total.subtract(discount));
            } else {
                vo.setUnusableReason("满 " + vo.getThreshold().stripTrailingZeros().toPlainString() + " 元可用");
            }
        }
        // 可使用在前，按抵扣金额从大到小
        list.sort(Comparator
                .comparing((UserCouponVO vo) -> !Boolean.TRUE.equals(vo.getUsable()))
                .thenComparing(vo -> vo.getCurrentDiscount() == null ? BigDecimal.ZERO : vo.getCurrentDiscount(),
                        Comparator.reverseOrder()));
        return list;
    }

    /** 库中 UNUSED 但已过有效期 → 动态展示 EXPIRED */
    private String displayStatus(UserCouponVO vo, LocalDateTime now) {
        if (Constants.COUPON_USED.equals(vo.getStatus())) {
            return Constants.COUPON_USED;
        }
        if (vo.getValidEndTime() != null && now.isAfter(vo.getValidEndTime())) {
            return Constants.COUPON_EXPIRED;
        }
        return Constants.COUPON_UNUSED;
    }

    /* ============================ 下单闭环 ============================ */

    @Override
    public CouponRedeemVO redeem(Long userCouponId, Long userId, BigDecimal totalAmount) {
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc == null || !userId.equals(uc.getUserId())) {
            throw new BusinessException("优惠券不存在");
        }
        if (!Constants.COUPON_UNUSED.equals(uc.getStatus())) {
            throw new BusinessException("该优惠券已使用或已失效");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(uc.getValidStartTime()) || now.isAfter(uc.getValidEndTime())) {
            throw new BusinessException("优惠券不在有效期内");
        }
        Coupon coupon = couponMapper.selectById(uc.getCouponId());
        if (coupon == null) {
            throw new BusinessException("优惠券规则不存在");
        }
        if (totalAmount == null || totalAmount.compareTo(coupon.getThreshold()) < 0) {
            throw new BusinessException("订单金额未达到该优惠券使用门槛（满 "
                    + coupon.getThreshold().stripTrailingZeros().toPlainString() + " 元可用）");
        }
        BigDecimal discount = calcDiscount(coupon, totalAmount);
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("当前订单无法使用该优惠券");
        }
        return new CouponRedeemVO(uc.getId(), coupon.getName(), discount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindUsed(Long userCouponId, Long userId, Long orderId) {
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc == null || !userId.equals(uc.getUserId())) {
            throw new BusinessException("优惠券不存在");
        }
        int rows = userCouponMapper.markUsed(userCouponId, userId, orderId);
        if (rows == 0) {
            // 并发下券已被其他订单占用
            throw new BusinessException("优惠券状态异常，请重新选择支付方式");
        }
        couponMapper.update(null, new LambdaUpdateWrapper<Coupon>()
                .eq(Coupon::getId, uc.getCouponId())
                .setSql("used_count = used_count + 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundByOrder(Long orderId) {
        // 先查券（可能订单未用券）
        UserCoupon uc = userCouponMapper.selectOne(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getOrderId, orderId)
                .eq(UserCoupon::getStatus, Constants.COUPON_USED));
        if (uc == null) {
            return;
        }
        int rows = userCouponMapper.markUnused(orderId);
        if (rows > 0) {
            couponMapper.update(null, new LambdaUpdateWrapper<Coupon>()
                    .eq(Coupon::getId, uc.getCouponId())
                    .setSql("used_count = GREATEST(used_count - 1, 0)"));
            log.info("订单 {} 取消，优惠券 {} 已退回", orderId, uc.getId());
        }
    }

    @Override
    public BigDecimal calcDiscount(Coupon coupon, BigDecimal totalAmount) {
        BigDecimal discount;
        if (Constants.COUPON_DISCOUNT.equals(coupon.getType())) {
            // 折扣券：discountPercent=88 → 优惠 = total × (100-88)/100
            BigDecimal rateHundred = new BigDecimal(100 - coupon.getDiscountPercent());
            discount = totalAmount.multiply(rateHundred)
                    .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            if (coupon.getMaxDiscountAmount() != null
                    && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                discount = coupon.getMaxDiscountAmount();
            }
        } else {
            // 代金券 / 满减券
            discount = coupon.getDiscountAmount();
        }
        // 保证实付至少 0.01 元
        BigDecimal maxDiscount = totalAmount.subtract(MIN_PAY);
        if (discount.compareTo(maxDiscount) > 0) {
            discount = maxDiscount;
        }
        if (discount.signum() < 0) {
            discount = BigDecimal.ZERO;
        }
        return discount.setScale(2, RoundingMode.HALF_UP);
    }
}
