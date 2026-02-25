package com.younghwan.lifelog.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    @Test
    void generateAndParse_containsExpectedClaims() {
        JwtTokenProvider provider = createProvider(60);
        UserAccount user = UserAccount.builder()
                .id(7L)
                .username("jwt-user")
                .role(UserRole.ADMIN)
                .status(UserStatus.APPROVED)
                .build();

        String token = provider.generateAccessToken(user);
        Claims claims = provider.parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo("jwt-user");
        Number uid = claims.get("uid", Number.class);
        assertThat(uid.longValue()).isEqualTo(7L);
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.get("status", String.class)).isEqualTo("APPROVED");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void parseClaims_throwsForExpiredToken() throws InterruptedException {
        JwtTokenProvider provider = createProvider(1);
        UserAccount user = UserAccount.builder()
                .id(1L)
                .username("soon-expired")
                .role(UserRole.USER)
                .status(UserStatus.APPROVED)
                .build();

        String token = provider.generateAccessToken(user);
        Thread.sleep(1200);

        assertThatThrownBy(() -> provider.parseClaims(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void parseClaims_throwsForInvalidToken() {
        JwtTokenProvider provider = createProvider(60);

        assertThatThrownBy(() -> provider.parseClaims("not-a-valid-token"))
                .isInstanceOf(JwtException.class);
    }

    private JwtTokenProvider createProvider(long expirySeconds) {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "secret", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(provider, "accessTokenExpirySeconds", expirySeconds);
        provider.init();
        return provider;
    }
}
