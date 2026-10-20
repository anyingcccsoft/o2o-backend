package com.shangda.o2o.controller;

import com.shangda.o2o.common.ApiResponse;
import com.shangda.o2o.dto.CartAddRequest;
import com.shangda.o2o.dto.CartItemVO;
import com.shangda.o2o.service.CartService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 购物车接口
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<List<CartItemVO>> list(HttpSession session) {
        return ApiResponse.success(cartService.listItems(currentUserId(session)));
    }

    @PostMapping
    public ApiResponse<Void> add(@Valid @RequestBody CartAddRequest request, HttpSession session) {
        cartService.addItem(currentUserId(session), request.getDishId(), request.getQuantity());
        return ApiResponse.success("已加入购物车", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateQuantity(@PathVariable Long id,
                                            @RequestBody Map<String, Integer> body,
                                            HttpSession session) {
        cartService.updateQuantity(currentUserId(session), id, body.get("quantity"));
        return ApiResponse.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> remove(@PathVariable Long id, HttpSession session) {
        cartService.removeItem(currentUserId(session), id);
        return ApiResponse.success("删除成功", null);
    }

    @DeleteMapping
    public ApiResponse<Void> clear(HttpSession session) {
        cartService.clear(currentUserId(session));
        return ApiResponse.success("购物车已清空", null);
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }
}
