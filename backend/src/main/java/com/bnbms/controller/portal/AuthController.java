package com.bnbms.controller.portal;

import com.bnbms.common.Result;
import com.bnbms.model.dto.LoginDTO;
import com.bnbms.model.dto.PasswordUpdateDTO;
import com.bnbms.model.dto.ProfileUpdateDTO;
import com.bnbms.model.dto.RegisterDTO;
import com.bnbms.model.vo.LoginVO;
import com.bnbms.model.vo.UserVO;
import com.bnbms.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/captcha")
    public void captcha(@RequestParam String key, HttpServletResponse response) {
        authService.writeCaptcha(key, response);
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok("登录成功", authService.login(dto));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
        authService.logout(token);
        return Result.ok();
    }

    @GetMapping("/profile")
    public Result<UserVO> profile() {
        return Result.ok(authService.currentProfile());
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody ProfileUpdateDTO dto) {
        return Result.ok(authService.updateProfile(dto));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordUpdateDTO dto) {
        authService.changePassword(dto);
        return Result.ok();
    }

    @PostMapping("/host-apply")
    public Result<Void> applyHost() {
        authService.applyHost();
        return Result.ok("申请已提交，请等待管理员审核", null);
    }
}
