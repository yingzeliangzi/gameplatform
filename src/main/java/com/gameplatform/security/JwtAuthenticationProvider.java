package com.gameplatform.security;

import com.gameplatform.model.dto.UserDTO;
import com.gameplatform.service.UserService;
import com.gameplatform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 15:11
 * @description TODO
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        String token = null;

        if (authentication.getCredentials() instanceof String &&
                ((String) authentication.getCredentials()).startsWith("Bearer ")) {
            token = ((String) authentication.getCredentials()).substring(7);
        }

        // 如果是token认证
        if (token != null) {
            if (!jwtUtil.validateToken(token)) {
                throw new BadCredentialsException("Invalid token");
            }
            username = jwtUtil.getUsernameFromToken(token);
            UserDTO user = userService.loadUserByUsername(username);
            return new UsernamePasswordAuthenticationToken(user, null,
                    user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList()));
        }

        // 如果是用户名密码认证
        UserDTO user = userService.loadUserByUsername(username);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return new UsernamePasswordAuthenticationToken(user, password,
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList()));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}