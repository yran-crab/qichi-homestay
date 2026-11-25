package com.bnbms.controller.portal;

import com.bnbms.common.PageResult;
import com.bnbms.common.Result;
import com.bnbms.model.dto.BookingCreateDTO;
import com.bnbms.model.dto.OrderQueryDTO;
import com.bnbms.model.vo.BookingConfirmVO;
import com.bnbms.model.vo.OrderVO;
import com.bnbms.security.UserContext;
import com.bnbms.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

/**
 * 租客订单：预订试算、下单、支付、取消、我的订单
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class PortalOrderController {

    private final BookingService bookingService;

    @PostMapping("/confirm")
    public Result<BookingConfirmVO> confirm(@Valid @RequestBody BookingCreateDTO dto) {
        return Result.ok(bookingService.confirm(UserContext.getUserId(), dto));
    }

    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody BookingCreateDTO dto) {
        return Result.ok("下单成功", bookingService.createOrder(UserContext.getUserId(), dto));
    }

    /** 发起支付宝沙箱支付，返回自动提交的支付表单 HTML */
    @PostMapping("/{id}/alipay")
    public Result<String> alipay(@PathVariable Long id) {
        return Result.ok(bookingService.startAlipayPay(UserContext.getUserId(), id));
    }

    /** 支付宝回跳后校验支付结果 */
    @PostMapping("/{id}/alipay/verify")
    public Result<Boolean> alipayVerify(@PathVariable Long id) {
        boolean paid = bookingService.verifyAlipayPay(UserContext.getUserId(), id);
        return paid ? Result.ok("支付成功", true) : Result.ok("支付未完成，请重试", false);
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        bookingService.cancel(UserContext.getUserId(), id, reason);
        return Result.ok("订单已取消", null);
    }

    @GetMapping("/my")
    public Result<PageResult<OrderVO>> myOrders(@ModelAttribute OrderQueryDTO query) {
        return Result.ok(bookingService.tenantPage(UserContext.getUserId(), query));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.ok(bookingService.getDetail(
                UserContext.getUserId(), UserContext.get().getRole(), id));
    }
}
