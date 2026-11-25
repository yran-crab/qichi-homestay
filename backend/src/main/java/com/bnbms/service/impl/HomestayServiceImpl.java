package com.bnbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bnbms.common.BusinessException;
import com.bnbms.common.Constants;
import com.bnbms.common.PageResult;
import com.bnbms.common.RedisHelper;
import com.bnbms.common.Result;
import com.bnbms.entity.Facility;
import com.bnbms.entity.Homestay;
import com.bnbms.entity.HomestayFacility;
import com.bnbms.entity.SysUser;
import com.bnbms.mapper.FacilityMapper;
import com.bnbms.mapper.HomestayFacilityMapper;
import com.bnbms.mapper.HomestayMapper;
import com.bnbms.mapper.SysUserMapper;
import com.bnbms.model.dto.HomestayQueryDTO;
import com.bnbms.model.dto.HomestaySaveDTO;
import com.bnbms.model.vo.HomestayCardVO;
import com.bnbms.model.vo.HomestayDetailVO;
import com.bnbms.security.UserContext;
import com.bnbms.service.FavoriteService;
import com.bnbms.service.HomestayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomestayServiceImpl implements HomestayService {

    private final HomestayMapper homestayMapper;
    private final HomestayFacilityMapper homestayFacilityMapper;
    private final FacilityMapper facilityMapper;
    private final SysUserMapper sysUserMapper;
    private final FavoriteService favoriteService;
    private final RedisHelper redisHelper;

    private static final Duration HOT_TTL = Duration.ofMinutes(10);

    /* ============================ 门户 ============================ */

    @Override
    public PageResult<HomestayCardVO> portalPage(HomestayQueryDTO q) {
        Page<Homestay> page = new Page<>(q.getPage(), q.getPageSize());
        queryPortalPage(page, q);
        List<HomestayCardVO> cards = page.getRecords().stream().map(HomestayCardVO::of).collect(Collectors.toList());
        fillFavored(cards, UserContext.getUserIdOrNull());
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), cards);
    }

    /** 批量填充当前用户收藏态 */
    private void fillFavored(List<HomestayCardVO> cards, Long userId) {
        if (cards.isEmpty() || userId == null) {
            return;
        }
        List<Long> ids = cards.stream().map(HomestayCardVO::getId).collect(Collectors.toList());
        Set<Long> favoredIds = favoriteService.findFavoredIds(userId, ids);
        cards.forEach(card -> card.setFavored(favoredIds.contains(card.getId())));
    }

    private void queryPortalPage(Page<Homestay> page, HomestayQueryDTO q) {
        homestayMapper.selectPortalPage(page,
                StringUtils.hasText(q.getKeyword()) ? q.getKeyword().trim() : null,
                StringUtils.hasText(q.getCity()) ? q.getCity().trim() : null,
                StringUtils.hasText(q.getType()) ? q.getType() : null,
                q.getMinPrice(), q.getMaxPrice(), q.getMinGuests(),
                q.getCheckIn(), q.getCheckOut(), q.getSort());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<HomestayCardVO> hotList(int limit) {
        List<HomestayCardVO> source;
        Object cached = redisHelper.get(Constants.REDIS_CACHE_HOT);
        if (cached instanceof List) {
            source = (List<HomestayCardVO>) cached;
        } else {
            List<Homestay> homestays = homestayMapper.selectHotList(Math.max(limit, 6));
            source = homestays.stream().map(HomestayCardVO::of).collect(Collectors.toList());
            redisHelper.set(Constants.REDIS_CACHE_HOT, source, HOT_TTL.toMinutes(), TimeUnit.MINUTES);
        }
        // 复制副本再填充个性化收藏态，避免污染缓存中的共享对象
        List<HomestayCardVO> result = source.stream().map(HomestayServiceImpl::copyCard).collect(Collectors.toList());
        fillFavored(result, UserContext.getUserIdOrNull());
        return result;
    }

    private static HomestayCardVO copyCard(HomestayCardVO src) {
        HomestayCardVO dst = new HomestayCardVO();
        org.springframework.beans.BeanUtils.copyProperties(src, dst);
        return dst;
    }

    @Override
    public HomestayDetailVO getPortalDetail(Long id, Long userId) {
        Homestay h = homestayMapper.selectById(id);
        if (h == null || !Constants.STAY_APPROVED.equals(h.getStatus())) {
            throw new BusinessException("房源不存在或已下架");
        }
        homestayMapper.incrementViewCount(id);
        h.setViewCount(h.getViewCount() + 1);
        HomestayDetailVO vo = assembleDetail(h);
        if (userId != null) {
            Set<Long> favoredIds = favoriteService.findFavoredIds(userId, new HashSet<>(java.util.Collections.singletonList(id)));
            vo.setFavored(favoredIds.contains(id));
        }
        return vo;
    }

    /* ============================ 房东 ============================ */

    @Override
    public PageResult<Homestay> hostPage(Long userId, String status, String keyword, long page, long pageSize) {
        QueryWrapper<Homestay> qw = new QueryWrapper<Homestay>()
                .eq("host_id", userId)
                .eq(StringUtils.hasText(status), "status", status)
                .and(StringUtils.hasText(keyword), w -> w.like("title", keyword).or().like("city", keyword))
                .orderByDesc("updated_at");
        Page<Homestay> p = homestayMapper.selectPage(new Page<>(page, pageSize), qw);
        return PageResult.of(p);
    }

    @Override
    public HomestayDetailVO getHostDetail(Long id, Long userId) {
        return assembleDetail(getOwnedHomestay(id, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createHomestay(Long userId, HomestaySaveDTO dto) {
        validateFacilities(dto.getFacilityIds());
        Homestay h = new Homestay();
        h.setHostId(userId);
        applySaveDto(h, dto);
        h.setStatus(Constants.STAY_PENDING);
        h.setViewCount(0);
        h.setFavoriteCount(0);
        h.setOrderCount(0);
        h.setRating(java.math.BigDecimal.ZERO);
        h.setReviewCount(0);
        homestayMapper.insert(h);
        replaceFacilities(h.getId(), dto.getFacilityIds());
        return h.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateHomestay(Long userId, Long id, HomestaySaveDTO dto) {
        Homestay h = getOwnedHomestay(id, userId);
        validateFacilities(dto.getFacilityIds());
        applySaveDto(h, dto);
        homestayMapper.updateById(h);
        replaceFacilities(id, dto.getFacilityIds());
        evictHotCache();
    }

    @Override
    public void submitAudit(Long userId, Long id) {
        Homestay h = getOwnedHomestay(id, userId);
        if (!Constants.STAY_REJECTED.equals(h.getStatus()) && !Constants.STAY_OFFLINE.equals(h.getStatus())) {
            throw new BusinessException("当前状态无需提交审核");
        }
        h.setStatus(Constants.STAY_PENDING);
        h.setAuditRemark(null);
        homestayMapper.updateById(h);
    }

    @Override
    public void offline(Long userId, Long id) {
        Homestay h = getOwnedHomestay(id, userId);
        if (!Constants.STAY_APPROVED.equals(h.getStatus())) {
            throw new BusinessException("仅已上架房源可下架");
        }
        h.setStatus(Constants.STAY_OFFLINE);
        homestayMapper.updateById(h);
        evictHotCache();
    }

    @Override
    public void online(Long userId, Long id) {
        Homestay h = getOwnedHomestay(id, userId);
        if (!Constants.STAY_OFFLINE.equals(h.getStatus())) {
            throw new BusinessException("仅已下架房源可重新上架");
        }
        h.setStatus(Constants.STAY_APPROVED);
        homestayMapper.updateById(h);
        evictHotCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeHomestay(Long userId, Long id) {
        Homestay h = getOwnedHomestay(id, userId);
        if (Constants.STAY_APPROVED.equals(h.getStatus())) {
            throw new BusinessException("请先下架房源再删除");
        }
        if (homestayMapper.countOrders(id) > 0) {
            throw new BusinessException("该房源存在订单记录，无法删除");
        }
        homestayFacilityMapper.delete(new QueryWrapper<HomestayFacility>().eq("homestay_id", id));
        homestayMapper.deleteById(id);
        evictHotCache();
    }

    /* ============================ 管理员 ============================ */

    @Override
    public PageResult<Homestay> adminPage(String status, String city, String keyword, long page, long pageSize) {
        QueryWrapper<Homestay> qw = new QueryWrapper<Homestay>()
                .eq(StringUtils.hasText(status), "status", status)
                .eq(StringUtils.hasText(city), "city", city)
                .and(StringUtils.hasText(keyword), w -> w.like("title", keyword).or().like("city", keyword))
                .orderByDesc("updated_at");
        Page<Homestay> p = homestayMapper.selectPage(new Page<>(page, pageSize), qw);
        return PageResult.of(p);
    }

    @Override
    public PageResult<Homestay> auditPage(long page, long pageSize) {
        Page<Homestay> p = homestayMapper.selectPage(new Page<>(page, pageSize),
                new QueryWrapper<Homestay>().eq("status", Constants.STAY_PENDING).orderByAsc("created_at"));
        return PageResult.of(p);
    }

    @Override
    public void audit(Long id, boolean approved, String remark) {
        Homestay h = homestayMapper.selectById(id);
        if (h == null) {
            throw new BusinessException("房源不存在");
        }
        if (!Constants.STAY_PENDING.equals(h.getStatus())) {
            throw new BusinessException("该房源不在待审核状态");
        }
        if (approved) {
            h.setStatus(Constants.STAY_APPROVED);
            h.setAuditRemark(null);
        } else {
            if (!StringUtils.hasText(remark)) {
                throw new BusinessException("驳回时必须填写原因");
            }
            h.setStatus(Constants.STAY_REJECTED);
            h.setAuditRemark(remark.trim());
        }
        homestayMapper.updateById(h);
        evictHotCache();
    }

    @Override
    public void forceOffline(Long id) {
        Homestay h = homestayMapper.selectById(id);
        if (h == null) {
            throw new BusinessException("房源不存在");
        }
        h.setStatus(Constants.STAY_OFFLINE);
        homestayMapper.updateById(h);
        evictHotCache();
    }

    /* ============================ 私有方法 ============================ */

    private Homestay getOwnedHomestay(Long id, Long userId) {
        Homestay h = homestayMapper.selectById(id);
        if (h == null) {
            throw new BusinessException("房源不存在");
        }
        if (!h.getHostId().equals(userId)) {
            throw new BusinessException(Result.CODE_FORBIDDEN, "无权操作他人房源");
        }
        return h;
    }

    private void applySaveDto(Homestay h, HomestaySaveDTO dto) {
        h.setTitle(dto.getTitle().trim());
        h.setCity(dto.getCity().trim());
        h.setAddress(dto.getAddress().trim());
        h.setType(dto.getType());
        h.setPrice(dto.getPrice());
        h.setOriginalPrice(dto.getOriginalPrice());
        h.setArea(dto.getArea());
        h.setBedrooms(dto.getBedrooms());
        h.setLivingRooms(dto.getLivingRooms());
        h.setBathrooms(dto.getBathrooms());
        h.setBeds(dto.getBeds());
        h.setMaxGuests(dto.getMaxGuests());
        h.setFloorInfo(dto.getFloorInfo());
        h.setDescription(dto.getDescription());
        h.setRules(dto.getRules());
        List<String> images = dto.getImages() == null ? new ArrayList<>() :
                dto.getImages().stream().filter(StringUtils::hasText).collect(Collectors.toList());
        if (images.size() > 10) {
            throw new BusinessException("房源图片最多 10 张");
        }
        h.setImages(String.join(",", images));
        if (StringUtils.hasText(dto.getCoverImg())) {
            h.setCoverImg(dto.getCoverImg());
        } else if (!images.isEmpty()) {
            h.setCoverImg(images.get(0));
        } else {
            throw new BusinessException("请至少上传一张房源图片");
        }
    }

    private void validateFacilities(List<Long> facilityIds) {
        if (CollectionUtils.isEmpty(facilityIds)) {
            throw new BusinessException("请至少选择一项配套设施");
        }
        Long count = facilityMapper.selectBatchIds(facilityIds).stream().count();
        if (count != facilityIds.stream().distinct().count()) {
            throw new BusinessException("配套设施数据不合法");
        }
    }

    private void replaceFacilities(Long homestayId, List<Long> facilityIds) {
        homestayFacilityMapper.delete(new QueryWrapper<HomestayFacility>().eq("homestay_id", homestayId));
        for (Long fid : facilityIds.stream().distinct().collect(Collectors.toList())) {
            HomestayFacility rel = new HomestayFacility();
            rel.setHomestayId(homestayId);
            rel.setFacilityId(fid);
            homestayFacilityMapper.insert(rel);
        }
    }

    private HomestayDetailVO assembleDetail(Homestay h) {
        HomestayDetailVO vo = HomestayDetailVO.of(h);
        List<HomestayFacility> rels = homestayFacilityMapper.selectList(
                new QueryWrapper<HomestayFacility>().eq("homestay_id", h.getId()));
        if (!rels.isEmpty()) {
            List<Long> fids = rels.stream().map(HomestayFacility::getFacilityId).collect(Collectors.toList());
            List<Facility> facilities = facilityMapper.selectBatchIds(fids).stream()
                    .sorted(Comparator.comparingInt(f -> f.getSort() == null ? 0 : f.getSort()))
                    .collect(Collectors.toList());
            vo.setFacilities(facilities);
        } else {
            vo.setFacilities(new ArrayList<>());
        }
        SysUser host = sysUserMapper.selectById(h.getHostId());
        if (host != null) {
            HomestayDetailVO.HostBrief brief = new HomestayDetailVO.HostBrief();
            brief.setId(host.getId());
            brief.setRealName(host.getRealName());
            brief.setAvatar(host.getAvatar());
            vo.setHost(brief);
        }
        return vo;
    }

    private void evictHotCache() {
        redisHelper.delete(Constants.REDIS_CACHE_HOT);
    }
}
