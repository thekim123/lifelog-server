package com.younghwan.lifelog.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserAccountRepository userAccountRepository;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void login_returnsTokenShapeForApprovedUser() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken("approved-user", "ignored");
        UserAccount approvedUser = UserAccount.builder()
                .id(10L)
                .username("approved-user")
                .passwordHash("hash")
                .role(UserRole.USER)
                .status(UserStatus.APPROVED)
                .build();

        given(authenticationManager.authenticate(any())).willReturn(auth);
        given(userAccountRepository.findByUsername("approved-user")).willReturn(Optional.of(approvedUser));
        given(jwtTokenProvider.generateAccessToken(approvedUser)).willReturn("jwt-token");
        given(jwtTokenProvider.getAccessTokenExpirySeconds()).willReturn(3600L);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"approved-user","password":"pw"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(3600))
                .andExpect(jsonPath("$.user.username").value("approved-user"))
                .andExpect(jsonPath("$.user.status").value("APPROVED"));
    }

    @Test
    void login_deniedWhenPendingApproval() throws Exception {
        given(authenticationManager.authenticate(any()))
                .willThrow(new DisabledException("User is pending approval"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"pending-user","password":"pw"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User is pending approval"));
    }

    @Test
    void login_deniedForInvalidCredentials() throws Exception {
        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"wrong","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }
}
