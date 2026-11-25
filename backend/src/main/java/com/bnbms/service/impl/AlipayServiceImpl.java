package com.bnbms.service.impl;

import cn.hutool.json.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.bnbms.common.BusinessException;
import com.bnbms.config.AlipayProperties;
import com.bnbms.service.AlipayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 支付宝沙箱支付服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayServiceImpl implements AlipayService {

    private final AlipayClient alipayClient;
    private final AlipayProperties alipayProperties;

    /** 密钥是否已配置 */
    private boolean keysConfigured() {
        return alipayProperties.getAppPrivateKey() != null && !alipayProperties.getAppPrivateKey().trim().isEmpty()
                && alipayProperties.getAlipayPublicKey() != null && !alipayProperties.getAlipayPublicKey().trim().isEmpty();
    }

    @Override
    public String createPagePay(String outTradeNo, BigDecimal totalAmount, String subject, String returnUrl) {
        if (!keysConfigured()) {
            log.warn("支付宝密钥未配置，无法发起支付: outTradeNo={}", outTradeNo);
            throw new BusinessException("支付宝沙箱密钥未配置（应用私钥/支付宝公钥），请联系管理员在 application.yml 中配置后再支付");
        }
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(returnUrl);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("total_amount", totalAmount.toPlainString());
        bizContent.put("subject", subject);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            String formHtml = response.getBody();
            log.info("创建支付宝支付表单成功: outTradeNo={}, amount={}", outTradeNo, totalAmount);
            return formHtml;
        } catch (AlipayApiException e) {
            log.error("创建支付宝支付表单失败: outTradeNo={}, code={}, msg={}",
                    outTradeNo, e.getErrCode(), e.getErrMsg(), e);
            throw new BusinessException("调用支付宝支付失败：" + e.getErrMsg());
        } catch (Exception e) {
            // 密钥格式错误、签名异常等非 AlipayApiException
            log.error("创建支付宝支付表单异常: outTradeNo={}", outTradeNo, e);
            throw new BusinessException("支付宝支付调用异常，请检查密钥配置是否正确：" + e.getMessage());
        }
    }

    @Override
    public boolean isTradeSuccess(String outTradeNo) {
        if (!keysConfigured()) {
            throw new BusinessException("支付宝沙箱密钥未配置，无法校验支付结果");
        }
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        request.setBizContent(bizContent.toString());

        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                log.warn("支付宝交易查询未成功: outTradeNo={}, code={}, msg={}, subCode={}, subMsg={}",
                        outTradeNo, response.getCode(), response.getMsg(),
                        response.getSubCode(), response.getSubMsg());
                return false;
            }
            String tradeStatus = response.getTradeStatus();
            log.info("支付宝交易状态: outTradeNo={}, tradeStatus={}", outTradeNo, tradeStatus);
            return "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
        } catch (AlipayApiException e) {
            log.error("支付宝交易查询异常: outTradeNo={}", outTradeNo, e);
            throw new BusinessException("支付查询失败：" + e.getErrMsg());
        } catch (Exception e) {
            log.error("支付宝交易查询异常: outTradeNo={}", outTradeNo, e);
            throw new BusinessException("支付查询异常，请检查密钥配置：" + e.getMessage());
        }
    }
}
