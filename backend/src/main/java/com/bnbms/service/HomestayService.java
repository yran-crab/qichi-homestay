package com.bnbms.service;

import com.bnbms.common.PageResult;
import com.bnbms.entity.Homestay;
import com.bnbms.model.dto.HomestayQueryDTO;
import com.bnbms.model.dto.HomestaySaveDTO;
import com.bnbms.model.vo.HomestayCardVO;
import com.bnbms.model.vo.HomestayDetailVO;

import java.util.List;

public interface HomestayService {

    /* ---------------- 门户 ---------------- */

    PageResult<HomestayCardVO> portalPage(HomestayQueryDTO query);

    List<HomestayCardVO> hotList(int limit);

    HomestayDetailVO getPortalDetail(Long id, Long userId);

    /* ---------------- 房东 ---------------- */

    PageResult<Homestay> hostPage(Long userId, String status, String keyword, long page, long pageSize);

    HomestayDetailVO getHostDetail(Long id, Long userId);

    Long createHomestay(Long userId, HomestaySaveDTO dto);

    void updateHomestay(Long userId, Long id, HomestaySaveDTO dto);

    void submitAudit(Long userId, Long id);

    void offline(Long userId, Long id);

    void online(Long userId, Long id);

    void removeHomestay(Long userId, Long id);

    /* ---------------- 管理员 ---------------- */

    PageResult<Homestay> adminPage(String status, String city, String keyword, long page, long pageSize);

    PageResult<Homestay> auditPage(long page, long pageSize);

    void audit(Long id, boolean approved, String remark);

    void forceOffline(Long id);
}
