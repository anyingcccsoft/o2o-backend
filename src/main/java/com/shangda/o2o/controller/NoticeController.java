package com.shangda.o2o.controller;

import com.shangda.o2o.common.ApiResponse;
import com.shangda.o2o.entity.Notice;
import com.shangda.o2o.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端公告接口
 */
@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final CatalogService catalogService;

    @GetMapping("/api/notices")
    public ApiResponse<List<Notice>> list() {
        return ApiResponse.success(catalogService.listPublishedNotices());
    }
}
