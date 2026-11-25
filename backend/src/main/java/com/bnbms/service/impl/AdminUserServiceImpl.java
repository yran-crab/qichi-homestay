package com.bnbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bnbms.common.BusinessException;
import com.bnbms.common.Constants;
import com.bnbms.common.PageResult;
import com.bnbms.entity.SysUser;
import com.bnbms.model.vo.UserVO;
import com.bnbms.security.UserContext;
import com.bnbms.service.AdminUserService;
import com.bnbms.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final SysUserService userService;

    @Override
    public PageResult<UserVO> userPage(long page, long pageSize, String role, Integer status, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(role)) {
            wrapper.eq(SysUser::getRole, role);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(SysUser::getUsername, kw)
                    .or().like(SysUser::getRealName, kw)
                    .or().like(SysUser::getPhone, kw));
        }
        wrapper.orderByDesc(SysUser::getCreatedAt);
        Page<SysUser> result = userService.page(new Page<>(page, pageSize), wrapper);
        List<UserVO> list = result.getRecords().stream().map(UserVO::of).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), list);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态参数非法");
        }
        if (id.equals(UserContext.getUserId())) {
            throw new BusinessException("不能禁用当前登录账号");
        }
        SysUser user = userService.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (Constants.ROLE_ADMIN.equals(user.getRole())) {
            throw new BusinessException("不允许禁用管理员账号");
        }
        SysUser update = new SysUser();
        update.setId(id);
        update.setStatus(status);
        userService.updateById(update);
    }

    @Override
    public PageResult<UserVO> hostApplyPage(long page, long pageSize, String realName) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getHostStatus, Constants.HOST_PENDING)
                .orderByDesc(SysUser::getUpdatedAt);
        if (StringUtils.hasText(realName)) {
            wrapper.like(SysUser::getRealName, realName.trim());
        }
        Page<SysUser> result = userService.page(new Page<>(page, pageSize), wrapper);
        List<UserVO> list = result.getRecords().stream().map(UserVO::of).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditHost(Long id, boolean approved) {
        SysUser user = userService.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!Constants.HOST_PENDING.equals(user.getHostStatus())) {
            throw new BusinessException("该申请已处理，请勿重复操作");
        }
        SysUser update = new SysUser();
        update.setId(id);
        if (approved) {
            update.setRole(Constants.ROLE_HOST);
            update.setHostStatus(Constants.HOST_APPROVED);
        } else {
            update.setHostStatus(Constants.HOST_REJECTED);
        }
        userService.updateById(update);
    }
}
