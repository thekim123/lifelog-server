package com.younghwan.lifelog.household;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/households")
@RequiredArgsConstructor
public class HouseholdController {
    private final HouseholdService householdService;

    @PostMapping
    public ResponseEntity<HouseholdDtos.HouseholdResponse> create(@Valid @RequestBody HouseholdDtos.CreateHouseholdRequest request) {
        return ResponseEntity.ok(householdService.create(request));
    }

    @GetMapping("/mine")
    public List<HouseholdDtos.HouseholdResponse> mine() {
        return householdService.myHouseholds();
    }

    @PostMapping("/{householdId}/members")
    public ResponseEntity<HouseholdDtos.HouseholdResponse> addMember(@PathVariable Long householdId,
                                                                     @Valid @RequestBody HouseholdDtos.AddMemberRequest request) {
        return ResponseEntity.ok(householdService.addMember(householdId, request));
    }

    @PutMapping("/{householdId}/members/{userId}/role")
    public ResponseEntity<HouseholdDtos.HouseholdResponse> updateMemberRole(@PathVariable Long householdId,
                                                                             @PathVariable Long userId,
                                                                             @Valid @RequestBody HouseholdDtos.UpdateMemberRoleRequest request) {
        return ResponseEntity.ok(householdService.updateMemberRole(householdId, userId, request));
    }

    @DeleteMapping("/{householdId}/members/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable Long householdId, @PathVariable Long userId) {
        householdService.removeMember(householdId, userId);
        return ResponseEntity.ok().build();
    }
}
