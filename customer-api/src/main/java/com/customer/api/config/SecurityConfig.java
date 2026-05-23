package com.customer.api.config;

import com.customer.api.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// TODO(security): Consider using HTTPS/TLS for production deployment.
// TODO(security): Consider implementing rate limiting on API endpoints.
@Configuration
public class SecurityConfig {

    @Bean
    public FilterRegistrationBean<BearerTokenFilter> bearerTokenFilter(AuthService authService) {
        FilterRegistrationBean<BearerTokenFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new BearerTokenFilter(authService));
        registration.addUrlPatterns("/customers/*", "/customers");
        registration.setOrder(1);
        return registration;
    }

    static class BearerTokenFilter extends OncePerRequestFilter {

        private final AuthService authService;

        BearerTokenFilter(AuthService authService) {
            this.authService = authService;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain)
                throws ServletException, IOException {

            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendUnauthorized(response, "Missing or invalid Authorization header");
                return;
            }

            String token = authHeader.substring(7).trim();

            if (!authService.validateToken(token)) {
                sendUnauthorized(response, "Invalid or expired token");
                return;
            }

            filterChain.doFilter(request, response);
        }

        private void sendUnauthorized(HttpServletResponse response, String message)
                throws IOException {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}");
        }
    }
}
