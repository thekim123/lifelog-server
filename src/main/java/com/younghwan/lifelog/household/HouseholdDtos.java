package com.younghwan.lifelog.household;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class HouseholdDtos {
    public record CreateHouseholdRequest(
            @NotBlank String name
    ) {}

    public record AddMemberRequest(
            @NotBlank String username,
            @NotNull HouseholdRole role
    ) {}

    public record UpdateMemberRoleRequest(
            @NotNull HouseholdRole role
    ) {}

    public record HouseholdMemberResponse(
            Long userId,
            String username,
            HouseholdRole role
    ) {
        public static HouseholdMemberResponse from(HouseholdMember member) {
            return new HouseholdMemberResponse(
                    member.getUser().getId(),
                    member.getUser().getUsername(),
                    member.getRole()
            );
        }
    }

    public record HouseholdResponse(
            Long id,
            String name,
            HouseholdRole myRole,
            List<HouseholdMemberResponse> members
    ) {}
}
