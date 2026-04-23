package com.practice.cursor.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.cursor.global.exception.ErrorCode;
import com.practice.cursor.global.response.ApiResponse;
import com.practice.cursor.global.service.TokenRedisService;
import com.practice.cursor.global.util.JwtUtil;
import com.practice.cursor.domain.member.entity.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 인증 필터.
 * Authorization 헤더의 Bearer 토큰을 검증하고 SecurityContext에 인증 정보를 설정한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final TokenRedisService tokenRedisService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);
            
            // 토큰이 없으면 다음 필터로 통과 (인증 불필요 엔드포인트 고려)
            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 토큰 유효성 검증
            jwtUtil.validateToken(token);

            // 블랙리스트 확인
            if (tokenRedisService.isBlacklisted(token)) {
                sendErrorResponse(response, ErrorCode.TOKEN_BLACKLISTED);
                return;
            }

            // 인증 정보 설정
            setAuthentication(token);
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.debug("만료된 토큰입니다: {}", e.getMessage());
            sendErrorResponse(response, ErrorCode.TOKEN_EXPIRED);
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            log.debug("유효하지 않은 토큰입니다: {}", e.getMessage());
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
        } catch (Exception e) {
            log.error("JWT 필터에서 예상치 못한 오류가 발생했습니다", e);
            sendErrorResponse(response, ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 요청에서 Bearer 토큰을 추출한다.
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    /**
     * JWT 토큰에서 인증 정보를 추출하여 SecurityContext에 설정한다.
     */
    private void setAuthentication(String token) {
        Long memberId = jwtUtil.extractMemberId(token);
        Role role = jwtUtil.extractRole(token);
        
        String roleName = "ROLE_" + role.name();
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleName));
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                memberId, 
                null, 
                authorities
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 에러 응답을 전송한다.
     */
    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> errorResponse = ApiResponse.fail(errorCode);
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        
        response.getWriter().write(jsonResponse);
    }
}
