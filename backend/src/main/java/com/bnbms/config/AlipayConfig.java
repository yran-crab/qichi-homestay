package com.bnbms.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝客户端 Bean 配置（公钥模式）
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AlipayConfig {

    private final AlipayProperties props;

    @Bean
    public AlipayClient alipayClient() {
        if (props.getAppPrivateKey() == null || props.getAppPrivateKey().trim().isEmpty()) {
            log.warn("支付宝应用私钥未配置（alipay.app-private-key），支付功能将不可用");
        }
        if (props.getAlipayPublicKey() == null || props.getAlipayPublicKey().trim().isEmpty()) {
            log.warn("支付宝公钥未配置（alipay.alipay-public-key），支付功能将不可用");
        }
        return new DefaultAlipayClient(
                props.getGateway(),
                props.getAppId(),
                props.getAppPrivateKey(),
                props.getFormat(),
                props.getCharset(),
                props.getAlipayPublicKey(),
                props.getSignType()
        );
    }
}
