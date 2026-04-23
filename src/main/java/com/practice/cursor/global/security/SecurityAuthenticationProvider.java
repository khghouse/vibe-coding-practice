package com.practice.cursor.global.security;

import com.practice.cursor.global.exception.CustomException;
import com.practice.cursor.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Spring Security AuthenticationProvider 구현체.
 * 사용자 인증을 처리한다.
 */
@Component
@RequiredArgsConstructor
public class SecurityAuthenticationProvider implements AuthenticationProvider {

    private final SecurityUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String loginId = authentication.getName();
        String password = (String) authentication.getCredentials();

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);
            
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
            }

            return new UsernamePasswordAuthenticationToken(
                    userDetails, 
                    null, 
                    userDetails.getAuthorities()
            );
        } catch (UsernameNotFoundException e) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
