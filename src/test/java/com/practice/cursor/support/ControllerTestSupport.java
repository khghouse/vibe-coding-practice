package com.practice.cursor.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import com.practice.cursor.domain.auth.service.AuthService;
import com.practice.cursor.domain.todo.service.TodoService;
import com.practice.cursor.global.config.SecurityConfig;
import jakarta.servlet.FilterChain;
import com.practice.cursor.global.security.JwtAuthenticationEntryPoint;
import com.practice.cursor.global.security.JwtAuthenticationFilter;
import com.practice.cursor.global.security.MemberAuthenticationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring Security 포함 Controller 슬라이스 테스트용 추상 클래스.
 * 
 * <p>{@code @WebMvcTest}로 Controller 레이어만 로딩하여 테스트한다.
 * 모든 MockBean은 부모 클래스에서 관리하여 자식 테스트 클래스의 설정을 최소화한다.
 */
@WebMvcTest
@ActiveProfiles("test")
@Import(SecurityConfig.class)
public abstract class ControllerTestSupport {

    @MockBean
    protected TodoService todoService;

    @MockBean
    protected AuthService authService;

    @MockBean
    protected JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    protected JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    protected MemberAuthenticationProvider memberAuthenticationProvider;

    @BeforeEach
    void setUpJwtAuthenticationFilter() throws Exception {
        doAnswer(invocation -> {
            FilterChain filterChain = invocation.getArgument(2);
            filterChain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }
}
