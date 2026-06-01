package com.vocaloid.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只拦截管理相关的API接口
        String uri = request.getRequestURI();
        
        // 定义需要拦截的管理接口路径
        boolean needAuth = false;
        
        // 管理页面相关接口需要验证
        if (uri.equals("/api/music/list") ||           // 获取歌曲列表
            uri.equals("/api/music/batch-upload") ||   // 批量上传
            uri.equals("/api/music/upload") ||         // 上传歌曲
            uri.startsWith("/api/music/") && 
            (uri.endsWith("/title") ||                 // 修改歌曲名称
             uri.equals("/api/music/all"))) {          // 删除所有歌曲
            needAuth = true;
        }
        
        // DELETE请求（删除单个歌曲）也需要验证
        if ("DELETE".equals(request.getMethod()) && uri.matches("/api/music/\\d+")) {
            needAuth = true;
        }
        
        if (needAuth) {
            // 获取请求头中的密钥
            String secretKey = request.getHeader("X-Admin-Secret");
            
            // 验证密钥
            if (secretKey == null || !secretKey.equals(SecurityConfig.ADMIN_SECRET_KEY)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("未授权访问：密钥无效");
                return false;
            }
        }
        
        return true;
    }
}
