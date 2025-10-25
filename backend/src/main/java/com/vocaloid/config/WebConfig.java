package com.vocaloid.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源映射 - 处理所有静态文件
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0); // 开发环境不缓存
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 页面路由已由PageController处理，这里不再重复配置
        // 保留测试页面的配置
        registry.addViewController("/test").setViewName("forward:/test.html");
        registry.addViewController("/api-test").setViewName("forward:/api-test.html");
        registry.addViewController("/debug").setViewName("forward:/debug.html");
        registry.addViewController("/guess-debug").setViewName("forward:/guess-debug.html");
    }
}
