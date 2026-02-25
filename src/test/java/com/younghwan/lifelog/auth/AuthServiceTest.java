package com.younghwan.lifelog.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void signup_createsPendingUserWithEncodedPassword() {
        AuthDtos.SignupRequest request = new AuthDtos.SignupRequest("new-user", "raw-pass");

        given(userAccountRepository.existsByUsername("new-user")).willReturn(false);
        given(passwordEncoder.encode("raw-pass")).willReturn("encoded-pass");
        given(userAccountRepository.save(any(UserAccount.class))).willAnswer(invocation -> {
            UserAccount saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        AuthDtos.UserResponse response = authService.signup(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("new-user");
        assertThat(response.role()).isEqualTo(UserRole.USER.name());
        assertThat(response.status()).isEqualTo(UserStatus.PENDING.name());
    }

    @Test
    void signup_throwsWhenUsernameAlreadyExists() {
        given(userAccountRepository.existsByUsername("dup-user")).willReturn(true);

        assertThatThrownBy(() -> authService.signup(new AuthDtos.SignupRequest("dup-user", "pw")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists");
    }
}
