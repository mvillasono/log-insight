package io.github.mvillasono.loginsight.autoconfigure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class LogInsightRequestFilter extends OncePerRequestFilter {

    public static final String MDC_HTTP_METHOD = "http.method";
    public static final String MDC_HTTP_PATH   = "http.path";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            MDC.put(MDC_HTTP_METHOD, request.getMethod());
            MDC.put(MDC_HTTP_PATH, request.getRequestURI());
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_HTTP_METHOD);
            MDC.remove(MDC_HTTP_PATH);
        }
    }
}
