package com.younghwan.lifelog.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthDtos.UserResponse signup(AuthDtos.SignupRequest request) {
        if (userAccountRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        UserAccount user = userAccountRepository.save(UserAccount.builder()
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .status(UserStatus.PENDING)
                .build());

        return AuthDtos.UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public AuthDtos.UserResponse currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipalDetails userPrincipal) {
            return new AuthDtos.UserResponse(
                    userPrincipal.getId(),
                    userPrincipal.getUsername(),
                    userPrincipal.getRole().name(),
                    userPrincipal.getStatus().name()
            );
        }

        return userAccountRepository.findByUsername(auth.getName())
                .map(AuthDtos.UserResponse::from)
                .orElse(null);
    }
}
