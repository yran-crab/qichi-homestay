package com.bnbms.model.vo;

import com.bnbms.entity.SysUser;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private String role;
    private String hostStatus;
    private Integer status;
    private LocalDateTime createdAt;

    public static UserVO of(SysUser u) {
        UserVO vo = new UserVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setRealName(u.getRealName());
        vo.setPhone(u.getPhone());
        vo.setEmail(u.getEmail());
        vo.setAvatar(u.getAvatar());
        vo.setRole(u.getRole());
        vo.setHostStatus(u.getHostStatus());
        vo.setStatus(u.getStatus());
        vo.setCreatedAt(u.getCreatedAt());
        return vo;
    }
}
