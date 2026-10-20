package com.shangda.o2o.controller;

import com.shangda.o2o.common.ApiResponse;
import com.shangda.o2o.common.PageResult;
import com.shangda.o2o.dto.OrderCreateRequest;
import com.shangda.o2o.dto.OrderVO;
import com.shangda.o2o.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用户端订单接口
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody OrderCreateRequest request,
                                                   HttpSession session) {
        return ApiResponse.success("下单成功", orderService.create(currentUserId(session), request));
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<Void> pay(@PathVariable Long id, HttpSession session) {
        orderService.pay(currentUserId(session), id);
        return ApiResponse.success("支付成功", null);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id, HttpSession session) {
        orderService.cancel(currentUserId(session), id);
        return ApiResponse.success("订单已取消", null);
    }

    @GetMapping
    public ApiResponse<PageResult<OrderVO>> page(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int pageSize,
                                                 HttpSession session) {
        return ApiResponse.success(orderService.page(currentUserId(session), page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderVO> detail(@PathVariable Long id, HttpSession session) {
        return ApiResponse.success(orderService.detail(currentUserId(session), id));
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }
}
