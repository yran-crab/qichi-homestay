package com.bnbms.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bnbms.common.BusinessException;
import com.bnbms.common.Constants;
import com.bnbms.common.RedisHelper;
import com.bnbms.entity.SysUser;
import com.bnbms.model.dto.LoginDTO;
import com.bnbms.model.dto.PasswordUpdateDTO;
import com.bnbms.model.dto.ProfileUpdateDTO;
import com.bnbms.model.dto.RegisterDTO;
import com.bnbms.model.vo.LoginVO;
import com.bnbms.model.vo.UserVO;
import com.bnbms.security.JwtUtil;
import com.bnbms.security.LoginUser;
import com.bnbms.security.UserContext;
import com.bnbms.service.AuthService;
import com.bnbms.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserService userService;
    private final JwtUtil jwtUtil;
    private final RedisHelper redisHelper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void writeCaptcha(String key, HttpServletResponse response) {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 15);
        String code = captcha.getCode();
        // Redis 不可用时验证码存储静默失败，登录时将跳过验证码校验
        redisHelper.set(Constants.REDIS_CAPTCHA_PREFIX + key,
                code.toLowerCase(), Duration.ofMinutes(5).toMillis(), TimeUnit.MILLISECONDS);
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache");
        try (OutputStream out = response.getOutputStream()) {
            captcha.write(out);
            out.flush();
        } catch (IOException e) {
            throw new BusinessException("验证码生成失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        Long nameCount = userService.count(new QueryWrapper<SysUser>().eq("username", dto.getUsername()));
        if (nameCount > 0) {
            throw new BusinessException("用户名已被注册");
        }
        Long phoneCount = userService.count(new QueryWrapper<SysUser>().eq("phone", dto.getPhone()));
        if (phoneCount > 0) {
            throw new BusinessException("手机号已被注册");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setRealName(dto.getRealName());
        user.setRole(Constants.ROLE_TENANT);
        user.setHostStatus(Constants.HOST_NONE);
        user.setStatus(1);
        userService.save(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // 验证码校验：Redis 不可用时静默跳过（验证码存不进去也取不出来）
        String captchaKey = Constants.REDIS_CAPTCHA_PREFIX + dto.getCaptchaKey();
        Object cachedCode = redisHelper.get(captchaKey);
        redisHelper.delete(captchaKey);
        if (cachedCode != null && !cachedCode.toString().equalsIgnoreCase(dto.getCaptcha().trim())) {
            throw new BusinessException("验证码错误或已过期");
        }

        SysUser user = userService.findWithPasswordByLoginName(dto.getUsername().trim());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole());
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), user.getRealName(), user.getRole());
        // Redis 不可用时 token 存储静默失败，AuthInterceptor 将走 JWT-only 模式
        redisHelper.set(Constants.REDIS_TOKEN_PREFIX + token, loginUser,
                jwtUtil.getExpireSeconds(), TimeUnit.SECONDS);
        return new LoginVO(token, UserVO.of(user));
    }

    @Override
    public void logout(String token) {
        if (token != null && !token.isEmpty()) {
            redisHelper.delete(Constants.REDIS_TOKEN_PREFIX + token);
        }
    }

    @Override
    public UserVO currentProfile() {
        SysUser user = userService.getById(UserContext.getUserId());
        if (user == null) {
            throw new BusinessException(401, "用户不存在或登录已失效");
        }
        return UserVO.of(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateProfile(ProfileUpdateDTO dto) {
        SysUser user = userService.getById(UserContext.getUserId());
        if (user == null) {
            throw new BusinessException(401, "登录已失效");
        }
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            Long count = userService.count(new QueryWrapper<SysUser>()
                    .eq("phone", dto.getPhone()).ne("id", user.getId()));
            if (count > 0) {
                throw new BusinessException("手机号已被其他账号使用");
            }
            user.setPhone(dto.getPhone());
        }
        if (dto.getRealName() != null && !dto.getRealName().isEmpty()) {
            user.setRealName(dto.getRealName());
        }
        user.setEmail(dto.getEmail());
        user.setAvatar(dto.getAvatar());
        userService.updateById(user);
        return UserVO.of(user);
    }

    @Override
    public void changePassword(PasswordUpdateDTO dto) {
        SysUser user = userService.findWithPasswordById(UserContext.getUserId());
        if (user == null || !passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userService.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyHost() {
        SysUser user = userService.getById(UserContext.getUserId());
        if (Constants.ROLE_HOST.equals(user.getRole())
                || Constants.HOST_APPROVED.equals(user.getHostStatus())) {
            throw new BusinessException("您已经是认证房东，无需重复申请");
        }
        if (Constants.HOST_PENDING.equals(user.getHostStatus())) {
            throw new BusinessException("入驻申请正在审核中，请勿重复提交");
        }
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setHostStatus(Constants.HOST_PENDING);
        userService.updateById(update);
    }
}
