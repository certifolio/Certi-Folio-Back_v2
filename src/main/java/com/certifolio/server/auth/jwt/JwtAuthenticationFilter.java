package com.certifolio.server.auth.jwt;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        try {
            String token = resolveToken(request);
            
            if (StringUtils.hasText(token)) {
                if(jwtTokenProvider.validateToken(token)) {
                    String subject = jwtTokenProvider.getSubject(token);
                    String role = jwtTokenProvider.getRole(token); 
                    
                    System.out.println("JwtAuthenticationFilter: Token valid. Subject=" + subject + ", Role=" + role);

                    Authentication authentication = new UsernamePasswordAuthenticationToken(subject, "",
                            Collections.singletonList(new SimpleGrantedAuthority(role != null ? role : "ROLE_USER")));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    System.out.println("JwtAuthenticationFilter: Token validation failed for " + path);
                }
            } else {
                 if (path.startsWith("/api")) {
                     System.out.println("JwtAuthenticationFilter: No token found for " + path);
                 }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
            System.out.println("JwtAuthenticationFilter: Exception " + ex.getMessage());
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
