package com.practice.cursor.global.security;

import com.practice.cursor.domain.member.entity.Member;
import com.practice.cursor.domain.member.entity.Role;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security UserDetails 구현체.
 * Member 엔티티를 감싸는 래퍼 클래스.
 */
@RequiredArgsConstructor
public class SecurityUser implements UserDetails {

    private final Member member;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = "ROLE_" + member.getRole().name();
        return List.of(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getLoginId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * 회원 ID를 반환한다.
     */
    public Long getMemberId() {
        return member.getId();
    }

    /**
     * 닉네임을 반환한다.
     */
    public String getNickname() {
        return member.getNickname();
    }

    /**
     * 권한을 반환한다.
     */
    public Role getRole() {
        return member.getRole();
    }

    /**
     * Member 엔티티를 반환한다.
     */
    public Member getMember() {
        return member;
    }
}
