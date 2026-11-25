package com.bnbms.service;

import com.bnbms.common.PageResult;
import com.bnbms.model.vo.UserVO;

public interface AdminUserService {

    PageResult<UserVO> userPage(long page, long pageSize, String role, Integer status, String keyword);

    void updateUserStatus(Long id, Integer status);

    PageResult<UserVO> hostApplyPage(long page, long pageSize, String realName);

    void auditHost(Long id, boolean approved);
}
