package com.bnbms.controller.host;

import com.bnbms.common.PageResult;
import com.bnbms.common.Result;
import com.bnbms.model.dto.OrderQueryDTO;
import com.bnbms.model.vo.OrderVO;
import com.bnbms.security.RequireRole;
import com.bnbms.security.UserContext;
import com.bnbms.service.BookingService;
import com.bnbms.common.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/host/orders")
@RequiredArgsConstructor
@RequireRole(Constants.ROLE_HOST)
public class HostOrderController {

    private final BookingService bookingService;

    @GetMapping
    public Result<PageResult<OrderVO>> page(@ModelAttribute OrderQueryDTO query) {
        return Result.ok(bookingService.hostPage(UserContext.getUserId(), query));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.ok(bookingService.getDetail(
                UserContext.getUserId(), Constants.ROLE_HOST, id));
    }

    @PutMapping("/{id}/check-in")
    public Result<Void> checkIn(@PathVariable Long id) {
        bookingService.checkIn(UserContext.getUserId(), id);
        return Result.ok("已确认入住", null);
    }

    @PutMapping("/{id}/check-out")
    public Result<Void> checkOut(@PathVariable Long id) {
        bookingService.checkOut(UserContext.getUserId(), id);
        return Result.ok("已办理退房", null);
    }
}
