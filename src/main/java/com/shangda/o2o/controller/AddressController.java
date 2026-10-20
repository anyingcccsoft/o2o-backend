package com.shangda.o2o.controller;

import com.shangda.o2o.common.ApiResponse;
import com.shangda.o2o.entity.Address;
import com.shangda.o2o.service.AddressService;
import jakarta.servlet.http.HttpSession;
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

/**
 * 收货地址接口
 */
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ApiResponse<List<Address>> list(HttpSession session) {
        return ApiResponse.success(addressService.list(currentUserId(session)));
    }

    @PostMapping
    public ApiResponse<Void> add(@RequestBody Address address, HttpSession session) {
        addressService.add(currentUserId(session), address);
        return ApiResponse.success("添加成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Address address,
                                    HttpSession session) {
        addressService.update(currentUserId(session), id, address);
        return ApiResponse.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpSession session) {
        addressService.delete(currentUserId(session), id);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/{id}/default")
    public ApiResponse<Void> setDefault(@PathVariable Long id, HttpSession session) {
        addressService.setDefault(currentUserId(session), id);
        return ApiResponse.success("已设为默认地址", null);
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }
}
