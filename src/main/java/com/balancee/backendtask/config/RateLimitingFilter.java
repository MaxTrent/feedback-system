package com.balancee.backendtask.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter implements Filter {
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> requestTimes = new ConcurrentHashMap<>();
    private final int maxRequests = 10; // 10 requests per minute
    private final long timeWindow = 60000; // 1 minute

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientIp = getClientIp(httpRequest);
        String endpoint = httpRequest.getRequestURI();
        
        // Only rate limit feedback submission
        if (!endpoint.equals("/api/feedback") || !httpRequest.getMethod().equals("POST")) {
            chain.doFilter(request, response);
            return;
        }
        
        String key = clientIp + ":" + endpoint;
        long currentTime = System.currentTimeMillis();
        
        requestTimes.compute(key, (k, lastTime) -> {
            if (lastTime == null || currentTime - lastTime > timeWindow) {
                requestCounts.put(k, new AtomicInteger(0));
                return currentTime;
            }
            return lastTime;
        });
        
        AtomicInteger count = requestCounts.get(key);
        if (count != null && count.incrementAndGet() > maxRequests) {
            httpResponse.setStatus(429);
            httpResponse.getWriter().write("{\"error\":\"Rate limit exceeded. Try again later.\"}");
            httpResponse.setContentType("application/json");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}