package com.xblog.common.config;

import com.xblog.common.intercepter.JwtInterceptor;
import com.xblog.common.util.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    public WebMvcConfig(JwtUtil jwtUtil) {
        this.jwtInterceptor = new JwtInterceptor(jwtUtil);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/v1/**")
                .excludePathPatterns(
                        "/v1/auth/login",
                        "/v1/auth/register",
                        "/v1/categories",
                        "/doc.html",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/favicon.ico"
                );
    }
}
