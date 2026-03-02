package com.example.blog.post.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilter() {
        FilterRegistrationBean<JwtAuthFilter> r = new FilterRegistrationBean<>();
        r.setFilter(new JwtAuthFilter());
        r.addUrlPatterns("/api/posts/admin/*", "/api/friend-links/admin/*", "/api/home-banners/admin/*");
        r.setOrder(1);
        return r;
    }
}
