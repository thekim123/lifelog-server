package com.younghwan.lifelog.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/approvals")
@RequiredArgsConstructor
public class AdminApprovalController {
    private final AdminApprovalService adminApprovalService;

    @GetMapping("/pending")
    public List<AuthDtos.UserResponse> pending() {
        return adminApprovalService.pending();
    }

    @PostMapping("/{userId}/approve")
    public ResponseEntity<AdminApprovalDtos.ApproveUserResponse> approve(@PathVariable Long userId) {
        return ResponseEntity.ok(adminApprovalService.approve(userId));
    }
}
