package com.younghwan.lifelog.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminApprovalService {
    private final UserAccountRepository userAccountRepository;

    @Transactional(readOnly = true)
    public List<AuthDtos.UserResponse> pending() {
        return userAccountRepository.findByStatus(UserStatus.PENDING)
                .stream()
                .map(AuthDtos.UserResponse::from)
                .toList();
    }

    @Transactional
    public AdminApprovalDtos.ApproveUserResponse approve(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setStatus(UserStatus.APPROVED);
        UserAccount saved = userAccountRepository.save(user);
        return AdminApprovalDtos.ApproveUserResponse.from(saved);
    }
}
