package com.practice.cursor.global.security;

import com.practice.cursor.domain.member.entity.Role;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security UserDetails 구현체.
 * Member 엔티티를 감싸는 래퍼 클래스.
 */
@Getter
public class MemberPrincipal implements UserDetails {

    private final Long memberId;
    private final String loginId;
    private final String password;
    private final String nickname;
    private final Role role;

    private MemberPrincipal(Long memberId, String loginId, String password, String nickname, Role role) {
        this.memberId = memberId;
        this.loginId = loginId;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }

    public static MemberPrincipal from(com.practice.cursor.domain.member.entity.Member member) {
        return new MemberPrincipal(
                member.getId(),
                member.getLoginId(),
                member.getPassword(),
                member.getNickname(),
                member.getRole());
    }

    public static MemberPrincipal authenticated(Long memberId, Role role) {
        return new MemberPrincipal(memberId, null, null, null, role);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = "ROLE_" + role.name();
        return List.of(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return loginId;
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

}
