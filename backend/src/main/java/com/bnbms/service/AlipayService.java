package com.bnbms.service;

import java.math.BigDecimal;

/**
 * 支付宝沙箱支付服务：封装 SDK 调用
 */
public interface AlipayService {

    /**
     * 创建电脑网站支付（alipay.trade.page.pay）表单 HTML
     *
     * @param outTradeNo  商户订单号（对应系统订单号）
     * @param totalAmount 支付金额
     * @param subject     订单标题
     * @param returnUrl   支付完成后浏览器回跳地址
     * @return 自动提交的支付表单 HTML
     */
    String createPagePay(String outTradeNo, BigDecimal totalAmount, String subject, String returnUrl);

    /**
     * 查询交易状态（alipay.trade.query），判断是否支付成功
     *
     * @param outTradeNo 商户订单号
     * @return true 表示交易状态为 TRADE_SUCCESS（已支付）
     */
    boolean isTradeSuccess(String outTradeNo);
}
