package com.younghwan.lifelog.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminApprovalController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminApprovalService adminApprovalService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserAccountRepository userAccountRepository;

    @Test
    void pending_returnsPendingUsers() throws Exception {
        given(adminApprovalService.pending()).willReturn(List.of(
                new AuthDtos.UserResponse(1L, "p1", "USER", "PENDING"),
                new AuthDtos.UserResponse(2L, "p2", "USER", "PENDING")
        ));

        mockMvc.perform(get("/api/admin/approvals/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("p1"))
                .andExpect(jsonPath("$[1].username").value("p2"));
    }

    @Test
    void approve_transitionsPendingToApproved() throws Exception {
        given(adminApprovalService.approve(9L))
                .willReturn(new AdminApprovalDtos.ApproveUserResponse(9L, "pending", "USER", "APPROVED"));

        mockMvc.perform(post("/api/admin/approvals/9/approve").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void approve_returnsBadRequestWhenUserMissing() throws Exception {
        given(adminApprovalService.approve(404L)).willThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(post("/api/admin/approvals/404/approve"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}
