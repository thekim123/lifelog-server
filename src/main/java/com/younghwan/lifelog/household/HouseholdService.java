package com.younghwan.lifelog.household;

import com.younghwan.lifelog.auth.UserAccount;
import com.younghwan.lifelog.auth.UserAccountRepository;
import com.younghwan.lifelog.auth.UserPrincipalDetails;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HouseholdService {
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final UserAccountRepository userAccountRepository;

    @Transactional
    public HouseholdDtos.HouseholdResponse create(HouseholdDtos.CreateHouseholdRequest request) {
        UserAccount me = currentUserAccount();

        Household household = householdRepository.save(Household.builder()
                .name(request.name())
                .build());

        HouseholdMember owner = householdMemberRepository.save(HouseholdMember.builder()
                .household(household)
                .user(me)
                .role(HouseholdRole.OWNER)
                .build());

        return new HouseholdDtos.HouseholdResponse(
                household.getId(),
                household.getName(),
                owner.getRole(),
                List.of(HouseholdDtos.HouseholdMemberResponse.from(owner))
        );
    }

    @Transactional(readOnly = true)
    public List<HouseholdDtos.HouseholdResponse> myHouseholds() {
        Long userId = currentUserId();
        return householdMemberRepository.findByUserId(userId).stream()
                .map(m -> toResponse(m.getHousehold(), m.getRole()))
                .sorted(Comparator.comparing(HouseholdDtos.HouseholdResponse::id))
                .toList();
    }

    @Transactional
    public HouseholdDtos.HouseholdResponse addMember(Long householdId, HouseholdDtos.AddMemberRequest request) {
        HouseholdMember me = requireMembership(householdId, currentUserId());
        if (me.getRole() == HouseholdRole.VIEWER) {
            throw new IllegalArgumentException("No permission to manage members");
        }

        UserAccount target = userAccountRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.username()));

        HouseholdMember member = householdMemberRepository.findByHouseholdIdAndUserId(householdId, target.getId())
                .map(existing -> {
                    existing.setRole(request.role());
                    return householdMemberRepository.save(existing);
                })
                .orElseGet(() -> householdMemberRepository.save(HouseholdMember.builder()
                        .household(me.getHousehold())
                        .user(target)
                        .role(request.role())
                        .build()));

        return toResponse(member.getHousehold(), me.getRole());
    }

    @Transactional
    public HouseholdDtos.HouseholdResponse updateMemberRole(Long householdId, Long userId, HouseholdDtos.UpdateMemberRoleRequest request) {
        HouseholdMember me = requireMembership(householdId, currentUserId());
        if (me.getRole() != HouseholdRole.OWNER) {
            throw new IllegalArgumentException("Only OWNER can change member role");
        }

        HouseholdMember target = requireMembership(householdId, userId);
        target.setRole(request.role());
        householdMemberRepository.save(target);
        return toResponse(target.getHousehold(), me.getRole());
    }

    @Transactional
    public void removeMember(Long householdId, Long userId) {
        HouseholdMember me = requireMembership(householdId, currentUserId());
        if (me.getRole() != HouseholdRole.OWNER) {
            throw new IllegalArgumentException("Only OWNER can remove member");
        }

        HouseholdMember target = requireMembership(householdId, userId);
        householdMemberRepository.delete(target);
    }

    @Transactional(readOnly = true)
    public HouseholdMember requireMembership(Long householdId, Long userId) {
        return householdMemberRepository.findByHouseholdIdAndUserId(householdId, userId)
                .orElseThrow(() -> new IllegalArgumentException("No access to household: " + householdId));
    }

    @Transactional(readOnly = true)
    public Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new IllegalArgumentException("Unauthorized");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipalDetails userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalArgumentException("Unauthorized");
    }

    @Transactional(readOnly = true)
    public UserAccount currentUserAccount() {
        Long id = currentUserId();
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("user not found: " + id));
    }

    private HouseholdDtos.HouseholdResponse toResponse(Household household, HouseholdRole myRole) {
        List<HouseholdDtos.HouseholdMemberResponse> members = householdMemberRepository.findByHouseholdId(household.getId())
                .stream().map(HouseholdDtos.HouseholdMemberResponse::from).toList();
        return new HouseholdDtos.HouseholdResponse(household.getId(), household.getName(), myRole, members);
    }
}
