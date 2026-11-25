package com.bnbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bnbms.common.BusinessException;
import com.bnbms.common.Constants;
import com.bnbms.common.PageResult;
import com.bnbms.common.Result;
import com.bnbms.entity.BookingOrder;
import com.bnbms.entity.Review;
import com.bnbms.mapper.BookingOrderMapper;
import com.bnbms.mapper.HomestayMapper;
import com.bnbms.mapper.ReviewMapper;
import com.bnbms.model.dto.ReviewCreateDTO;
import com.bnbms.model.dto.ReviewQueryDTO;
import com.bnbms.model.vo.ReviewVO;
import com.bnbms.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final BookingOrderMapper bookingOrderMapper;
    private final HomestayMapper homestayMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewVO create(Long tenantId, ReviewCreateDTO dto) {
        BookingOrder order = bookingOrderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getTenantId().equals(tenantId)) {
            throw new BusinessException(Result.CODE_FORBIDDEN, "无权评价该订单");
        }
        if (!Constants.ORDER_COMPLETED.equals(order.getStatus())) {
            throw new BusinessException("仅已完成订单可以评价");
        }
        if (order.getReviewed() != null && order.getReviewed() == 1) {
            throw new BusinessException("该订单已评价，不能重复评价");
        }
        // 唯一键 uk_order 兜底防并发重复
        Review review = new Review();
        review.setOrderId(order.getId());
        review.setHomestayId(order.getHomestayId());
        review.setTenantId(tenantId);
        review.setRating(dto.getRating());
        review.setContent(dto.getContent().trim());
        if (!CollectionUtils.isEmpty(dto.getImages())) {
            review.setImages(String.join(",", dto.getImages()));
        }
        try {
            reviewMapper.insert(review);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new BusinessException("该订单已评价，不能重复评价");
        }
        // 标记订单已评价
        bookingOrderMapper.update(null, new LambdaUpdateWrapper<BookingOrder>()
                .eq(BookingOrder::getId, order.getId())
                .set(BookingOrder::getReviewed, 1));
        // 重算房源评分与评价数
        homestayMapper.recalcRating(order.getHomestayId());
        return toVo(reviewMapper.selectById(review.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reply(Long hostId, Long reviewId, String content) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        // 校验房东身份：通过房源 host_id
        Long homestayId = review.getHomestayId();
        com.bnbms.entity.Homestay homestay = homestayMapper.selectById(homestayId);
        if (homestay == null || !homestay.getHostId().equals(hostId)) {
            throw new BusinessException(Result.CODE_FORBIDDEN, "无权回复该评价");
        }
        if (StringUtils.hasText(review.getReply())) {
            throw new BusinessException("该评价已回复，不能重复回复");
        }
        reviewMapper.update(null, new LambdaUpdateWrapper<Review>()
                .eq(Review::getId, reviewId)
                .set(Review::getReply, content.trim())
                .set(Review::getReplyAt, LocalDateTime.now()));
    }

    @Override
    public PageResult<ReviewVO> homestayPage(Long homestayId, long page, long pageSize) {
        Page<ReviewVO> p = new Page<>(page, pageSize);
        reviewMapper.selectHomestayPage(p, homestayId);
        p.getRecords().forEach(this::fillImages);
        return PageResult.of(p);
    }

    @Override
    public PageResult<ReviewVO> myPage(Long tenantId, long page, long pageSize) {
        Page<ReviewVO> p = new Page<>(page, pageSize);
        reviewMapper.selectMyPage(p, tenantId);
        p.getRecords().forEach(this::fillImages);
        return PageResult.of(p);
    }

    @Override
    public PageResult<ReviewVO> hostPage(Long hostId, ReviewQueryDTO query) {
        Page<ReviewVO> p = new Page<>(query.getPage(), query.getPageSize());
        reviewMapper.selectHostPage(p, hostId, query);
        p.getRecords().forEach(this::fillImages);
        return PageResult.of(p);
    }

    @Override
    public PageResult<ReviewVO> adminPage(ReviewQueryDTO query) {
        Page<ReviewVO> p = new Page<>(query.getPage(), query.getPageSize());
        reviewMapper.selectAdminPage(p, query);
        p.getRecords().forEach(this::fillImages);
        return PageResult.of(p);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        Long homestayId = review.getHomestayId();
        Long orderId = review.getOrderId();
        reviewMapper.deleteById(reviewId);
        // 重置订单已评价标记，允许重新评价
        bookingOrderMapper.update(null, new LambdaUpdateWrapper<BookingOrder>()
                .eq(BookingOrder::getId, orderId)
                .set(BookingOrder::getReviewed, 0));
        // 同步重算房源评分与评价数
        homestayMapper.recalcRating(homestayId);
    }

    /* ============================ 私有方法 ============================ */

    private void fillImages(ReviewVO vo) {
        if (vo == null || vo.getImagesRaw() == null) {
            return;
        }
        List<String> imgs = Arrays.stream(vo.getImagesRaw().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        vo.setImages(imgs);
    }

    private ReviewVO toVo(Review review) {
        if (review == null) {
            return null;
        }
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setOrderId(review.getOrderId());
        vo.setHomestayId(review.getHomestayId());
        vo.setTenantId(review.getTenantId());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setReply(review.getReply());
        vo.setReplyAt(review.getReplyAt());
        vo.setCreatedAt(review.getCreatedAt());
        if (StringUtils.hasText(review.getImages())) {
            vo.setImagesRaw(review.getImages());
            fillImages(vo);
        }
        return vo;
    }
}
