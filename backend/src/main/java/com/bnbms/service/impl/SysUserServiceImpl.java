package com.bnbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bnbms.entity.SysUser;
import com.bnbms.mapper.SysUserMapper;
import com.bnbms.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public SysUser findWithPasswordByLoginName(String loginName) {
        return getBaseMapper().selectOne(new QueryWrapper<SysUser>()
                .select("*")
                .eq("username", loginName)
                .or()
                .eq("phone", loginName)
                .last("LIMIT 1"));
    }

    @Override
    public SysUser findWithPasswordById(Long id) {
        return getBaseMapper().selectOne(new QueryWrapper<SysUser>()
                .select("*")
                .eq("id", id)
                .last("LIMIT 1"));
    }
}
