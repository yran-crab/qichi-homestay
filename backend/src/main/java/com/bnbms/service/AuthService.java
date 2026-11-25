package com.bnbms.service;

import com.bnbms.model.dto.LoginDTO;
import com.bnbms.model.dto.PasswordUpdateDTO;
import com.bnbms.model.dto.ProfileUpdateDTO;
import com.bnbms.model.dto.RegisterDTO;
import com.bnbms.model.vo.LoginVO;
import com.bnbms.model.vo.UserVO;

import javax.servlet.http.HttpServletResponse;

public interface AuthService {

    void writeCaptcha(String key, HttpServletResponse response);

    void register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    void logout(String token);

    UserVO currentProfile();

    UserVO updateProfile(ProfileUpdateDTO dto);

    void changePassword(PasswordUpdateDTO dto);

    void applyHost();
}
