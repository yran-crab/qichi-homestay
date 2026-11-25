package com.bnbms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝沙箱配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {

    /** 应用 APPID */
    private String appId;
    /** 支付宝网关地址 */
    private String gateway;
    /** 应用私钥（PKCS8 格式，纯文本） */
    private String appPrivateKey;
    /** 支付宝公钥 */
    private String alipayPublicKey;
    /** 签名类型 */
    private String signType = "RSA2";
    /** 编码 */
    private String charset = "UTF-8";
    /** 数据格式 */
    private String format = "json";
    /** 支付完成后浏览器回跳基础地址（前端地址） */
    private String returnUrlBase;
}
