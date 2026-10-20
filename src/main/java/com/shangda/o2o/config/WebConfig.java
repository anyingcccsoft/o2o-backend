package com.shangda.o2o.config;

import com.shangda.o2o.interceptor.AdminAuthInterceptor;
import com.shangda.o2o.interceptor.ApiCacheControlInterceptor;
import com.shangda.o2o.interceptor.UserAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC 配置：登录拦截器注册 + 上传目录资源映射
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserAuthInterceptor userAuthInterceptor;
    private final AdminAuthInterceptor adminAuthInterceptor;
    private final ApiCacheControlInterceptor apiCacheControlInterceptor;

    @Value("${o2o.upload-dir:./upload/}")
    private String uploadDir;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // /admin 重定向到 /admin/，保证页面内相对资源路径（css/admin.css 等）正确解析
        registry.addRedirectViewController("/admin", "/admin/");
        // /admin/ 指向 static/admin/index.html
        registry.addViewController("/admin/").setViewName("forward:/admin/index.html");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 所有 API 响应禁止浏览器缓存，避免展示旧数据
        registry.addInterceptor(apiCacheControlInterceptor)
                .addPathPatterns("/api/**");
        // 用户端：拦截 /api/**，放行公开接口与管理端（管理端由独立拦截器负责）
        registry.addInterceptor(userAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/register",
                        "/api/user/login",
                        "/api/categories",
                        "/api/dishes/**",
                        "/api/notices",
                        "/api/admin/**"
                );
        // 管理端：拦截 /api/admin/**，放行登录接口
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/login");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /upload/** 映射到本地上传目录（绝对路径，兼容相对配置）
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().normalize().toString();
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }
}
