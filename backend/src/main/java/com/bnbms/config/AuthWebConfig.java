package com.bnbms.config;

import com.bnbms.security.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 鉴权拦截器注册与匿名白名单
 */
@Configuration
@RequiredArgsConstructor
public class AuthWebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/health",
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/captcha",
                        // 门户公开浏览接口（/api/homestays 系列由 @OptionalAuth 处理，登录用户带收藏态）
                        "/api/home",
                        "/api/homestays/*/reviews",
                        "/api/homestays/*/booked-dates",
                        "/api/facilities",
                        "/api/ai/chat",
                        "/api/notices",
                        "/api/notices/*"
                );
    }
}
