package com.bnbms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bnbms.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    /** 按用户名或手机号查询用户（带密码字段） */
    SysUser findWithPasswordByLoginName(String loginName);

    /** 按 ID 查询含密码字段的用户 */
    SysUser findWithPasswordById(Long id);
}
