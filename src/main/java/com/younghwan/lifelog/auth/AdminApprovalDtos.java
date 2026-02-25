package com.younghwan.lifelog.auth;

public class AdminApprovalDtos {
    public record ApproveUserResponse(
            Long id,
            String username,
            String role,
            String status
    ) {
        public static ApproveUserResponse from(UserAccount user) {
            return new ApproveUserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getRole().name(),
                    user.getStatus().name()
            );
        }
    }
}
