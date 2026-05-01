package com.xblog.common.intercepter;

import com.alibaba.fastjson2.JSON;
import com.xblog.common.enums.ResultCode;
import com.xblog.common.util.JwtUtil;
import com.xblog.common.util.UserContext;
import com.xblog.entity.Result;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // OPTIONS 预检请求放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");

        // 1. 校验 header 格式
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "Token 无效");
            return false;
        }

        // 2. 提取 token
        String token = authHeader.substring(7);
        if (token.isEmpty()) {
            writeUnauthorized(response, "Token 无效");
            return false;
        }

        // 3. 解析 token
        try {
            Claims claims = jwtUtil.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            // 4. 存入 UserContext
            UserContext.set(userId, username, role);
            return true;
        } catch (JwtException e) {
            log.warn("JWT token 校验失败: {}", e.getMessage());
            writeUnauthorized(response, "Token 无效");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = new Result<>();
        result.setCode(ResultCode.AUTH_TOKEN_INVALID.getCode());
        result.setMessage(message);
        response.getWriter().write(JSON.toJSONString(result));
    }
}
