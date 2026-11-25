package com.bnbms.security;

import com.bnbms.common.BusinessException;
import com.bnbms.common.Result;

/**
 * 当前登录用户 ThreadLocal
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "未登录");
        }
        return user.getId();
    }

    /** 匿名可访问接口使用：未登录返回 null，不抛异常 */
    public static Long getUserIdOrNull() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
