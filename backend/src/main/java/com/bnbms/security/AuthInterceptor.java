package com.bnbms.security;

import com.bnbms.common.Constants;
import com.bnbms.common.RedisHelper;
import com.bnbms.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 登录与角色鉴权拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final RedisHelper redisHelper;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = resolveToken(request);
        if (token == null) {
            if (isOptionalAuth(handler)) {
                return true;
            }
            writeError(response, HttpStatus.UNAUTHORIZED.value(), "未登录或登录已过期");
            return false;
        }

        boolean optionalAuth = isOptionalAuth(handler);

        Claims claims;
        try {
            claims = jwtUtil.parse(token);
        } catch (Exception e) {
            if (optionalAuth) {
                return true;
            }
            writeError(response, HttpStatus.UNAUTHORIZED.value(), "登录令牌无效，请重新登录");
            return false;
        }

        String redisKey = Constants.REDIS_TOKEN_PREFIX + token;
        Object cached = redisHelper.get(redisKey);

        LoginUser loginUser;
        if (cached instanceof LoginUser) {
            // Redis 校验成功 + 滑动续期
            loginUser = (LoginUser) cached;
            redisHelper.expire(redisKey, jwtUtil.getExpireSeconds(), TimeUnit.SECONDS);
        } else {
            // Redis 不可用或 token 未存储：从 JWT claims 构造（降级模式）
            try {
                Long userId = claims.get("userId", Long.class);
                String username = claims.get("username", String.class);
                String role = claims.get("role", String.class);
                loginUser = new LoginUser(userId, username, null, role);
                log.info("Redis 不可用，走 JWT-only 降级模式: userId={}", userId);
            } catch (Exception e) {
                if (optionalAuth) {
                    return true;
                }
                writeError(response, HttpStatus.UNAUTHORIZED.value(), "登录状态已失效，请重新登录");
                return false;
            }
        }

        UserContext.set(loginUser);

        // 角色校验
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole != null && Arrays.stream(requireRole.value()).noneMatch(r -> r.equals(loginUser.getRole()))) {
            writeError(response, HttpStatus.FORBIDDEN.value(), "没有访问权限");
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private boolean isOptionalAuth(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return false;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        return handlerMethod.getMethodAnnotation(OptionalAuth.class) != null
                || handlerMethod.getBeanType().getAnnotation(OptionalAuth.class) != null;
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return null;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try (PrintWriter writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(Result.fail(status, message)));
            writer.flush();
        }
    }
}
