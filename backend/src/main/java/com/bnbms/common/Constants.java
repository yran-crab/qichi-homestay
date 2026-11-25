package com.bnbms.common;

/**
 * 系统常量：角色、业务状态、Redis key 前缀
 */
public interface Constants {

    String ROLE_ADMIN = "ADMIN";
    String ROLE_HOST = "HOST";
    String ROLE_TENANT = "TENANT";

    String HOST_NONE = "NONE";
    String HOST_PENDING = "PENDING";
    String HOST_APPROVED = "APPROVED";
    String HOST_REJECTED = "REJECTED";

    /** 房源状态 */
    String STAY_PENDING = "PENDING";
    String STAY_APPROVED = "APPROVED";
    String STAY_REJECTED = "REJECTED";
    String STAY_OFFLINE = "OFFLINE";

    /** 订单状态 */
    String ORDER_PENDING_PAY = "PENDING_PAY";
    String ORDER_PAID = "PAID";
    String ORDER_CHECKED_IN = "CHECKED_IN";
    String ORDER_COMPLETED = "COMPLETED";
    String ORDER_CANCELLED = "CANCELLED";

    /** 优惠券类型 */
    String COUPON_CASH = "CASH";
    String COUPON_FULL_REDUCE = "FULL_REDUCE";
    String COUPON_DISCOUNT = "DISCOUNT";
    /** 用户优惠券状态 */
    String COUPON_UNUSED = "UNUSED";
    String COUPON_USED = "USED";
    String COUPON_EXPIRED = "EXPIRED";
    /** 有效期类型 */
    String COUPON_VALID_FIXED = "FIXED";
    String COUPON_VALID_DAYS = "DAYS";

    String REDIS_TOKEN_PREFIX = "auth:token:";
    String REDIS_CAPTCHA_PREFIX = "auth:captcha:";
    String REDIS_LOCK_HOMESTAY = "lock:homestay:";
    String REDIS_CACHE_HOT = "cache:homestay:hot";
    String REDIS_CACHE_HOME = "cache:portal:home";
    String REDIS_CACHE_ADMIN_DASHBOARD = "cache:dashboard:admin";
    String REDIS_CACHE_HOST_DASHBOARD_PREFIX = "cache:dashboard:host:";
}
