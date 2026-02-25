package com.younghwan.lifelog.auth;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_setsAuthenticationForApprovedUser() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        var claims = io.jsonwebtoken.Jwts.claims().setSubject("approved-user");
        UserAccount user = UserAccount.builder()
                .id(3L)
                .username("approved-user")
                .passwordHash("hash")
                .role(UserRole.USER)
                .status(UserStatus.APPROVED)
                .build();

        given(jwtTokenProvider.parseClaims("valid-token")).willReturn(claims);
        given(userAccountRepository.findByUsername("approved-user")).willReturn(Optional.of(user));

        jwtAuthenticationFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertThat(principal).isInstanceOf(UserPrincipalDetails.class);
        assertThat(((UserPrincipalDetails) principal).getUsername()).isEqualTo("approved-user");
    }

    @Test
    void doFilter_doesNotAuthenticatePendingUser() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        var claims = io.jsonwebtoken.Jwts.claims().setSubject("pending-user");
        UserAccount user = UserAccount.builder()
                .id(4L)
                .username("pending-user")
                .passwordHash("hash")
                .role(UserRole.USER)
                .status(UserStatus.PENDING)
                .build();

        given(jwtTokenProvider.parseClaims("valid-token")).willReturn(claims);
        given(userAccountRepository.findByUsername("pending-user")).willReturn(Optional.of(user));

        jwtAuthenticationFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_clearsContextForInvalidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");

        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("x", null)
        );

        given(jwtTokenProvider.parseClaims("invalid-token")).willThrow(new JwtException("bad token"));

        jwtAuthenticationFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_ignoresWhenBearerHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        jwtAuthenticationFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        verify(jwtTokenProvider, never()).parseClaims(org.mockito.ArgumentMatchers.anyString());
    }
}
