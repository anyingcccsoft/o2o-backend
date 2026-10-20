package com.shangda.o2o.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * API 缓存控制拦截器：所有 /api/** 响应统一携带 Cache-Control: no-store，
 * 避免浏览器缓存动态接口数据导致页面展示旧值
 */
@Component
public class ApiCacheControlInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        response.setHeader("Cache-Control", "no-store");
        return true;
    }
}
