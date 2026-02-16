package com.certifolio.server.auth.jwt;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        // Log for profile endpoint specifically
        if (uri.contains("/portfolio/profile")) {
            System.out.println("=== DEBUG /api/portfolio/profile ===");
            System.out.println("Authorization header present: " + (authHeader != null));
            if (authHeader != null) {
                System.out.println("Authorization header starts with Bearer: " + authHeader.startsWith("Bearer "));
                System.out.println("Token length: " + (authHeader.length() > 7 ? authHeader.substring(7).length() : 0));
            }
        }

        String token = resolveToken(request);

        if (token != null) {
            System.out.println("JwtAuthenticationFilter: Token found for URI: " + uri);
            if (jwtTokenProvider.validateToken(token)) {
                System.out.println("JwtAuthenticationFilter: Token is valid");
                String subject = jwtTokenProvider.getSubject(token);
                System.out.println("JwtAuthenticationFilter: Subject = " + subject);
                Authentication authentication = new UsernamePasswordAuthenticationToken(subject, "",
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                System.out.println("JwtAuthenticationFilter: Token validation failed for URI: " + uri);
            }
        } else {
            if (uri.contains("/api/")) {
                System.out.println("JwtAuthenticationFilter: No token found for API URI: " + uri);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
