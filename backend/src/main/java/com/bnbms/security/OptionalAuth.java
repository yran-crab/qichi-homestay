package com.bnbms.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 匿名可访问接口：无 token 放行（UserContext 为空）；携带 token 时正常解析登录态。
 * 用于门户公开但需要按登录用户返回个性化数据（如收藏态）的接口。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionalAuth {
}
