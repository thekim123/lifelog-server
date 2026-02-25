package com.younghwan.lifelog.household;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HouseholdMemberRepository extends JpaRepository<HouseholdMember, Long> {
    List<HouseholdMember> findByUserId(Long userId);

    List<HouseholdMember> findByHouseholdId(Long householdId);

    Optional<HouseholdMember> findByHouseholdIdAndUserId(Long householdId, Long userId);

    boolean existsByHouseholdIdAndUserId(Long householdId, Long userId);
}
