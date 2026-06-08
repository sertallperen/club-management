package com.clubmanagement.service;

import com.clubmanagement.dto.common.PagedResponse;
import com.clubmanagement.dto.leave.LeaveRequestDto;
import com.clubmanagement.dto.leave.LeaveRequestResponse;
import com.clubmanagement.dto.leave.LeaveReviewRequest;
import com.clubmanagement.entity.LeaveRequest;
import com.clubmanagement.entity.User;
import com.clubmanagement.enums.RequestStatus;
import com.clubmanagement.enums.RoleName;
import com.clubmanagement.exception.BusinessRuleException;
import com.clubmanagement.exception.ResourceNotFoundException;
import com.clubmanagement.repository.LeaveRequestRepository;
import com.clubmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRepository;
    private final UserRepository userRepository;

    public PagedResponse<LeaveRequestResponse> getAll(Pageable pageable) {
        User current = getCurrentUser();
        if (current.getRole() == RoleName.PLAYER) {
            List<LeaveRequestResponse> list = leaveRepository
                    .findByRequestedByAndDeletedFalseOrderByCreatedAtDesc(current)
                    .stream().map(this::toResponse).toList();
            return PagedResponse.<LeaveRequestResponse>builder()
                    .content(list).page(0).size(list.size())
                    .totalElements(list.size()).totalPages(1).last(true).build();
        }
        return PagedResponse.of(leaveRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    public LeaveRequestResponse getById(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public LeaveRequestResponse create(LeaveRequestDto request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessRuleException("End date cannot be before start date");
        }
        User current = getCurrentUser();
        LeaveRequest leave = LeaveRequest.builder()
                .requestedBy(current)
                .reason(request.getReason())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(RequestStatus.PENDING)
                .build();
        return toResponse(leaveRepository.save(leave));
    }

    @Transactional
    public LeaveRequestResponse review(Long id, LeaveReviewRequest request) {
        LeaveRequest leave = findActive(id);
        if (leave.getStatus() != RequestStatus.PENDING) {
            throw new BusinessRuleException("Leave request has already been reviewed");
        }
        User reviewer = getCurrentUser();
        leave.setStatus(request.getDecision());
        leave.setReviewedBy(reviewer);
        leave.setReviewNote(request.getReviewNote());
        return toResponse(leaveRepository.save(leave));
    }

    @Transactional
    public void softDelete(Long id) {
        LeaveRequest leave = findActive(id);
        leave.setDeleted(true);
        leaveRepository.save(leave);
    }

    private LeaveRequest findActive(Long id) {
        return leaveRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", id));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new BusinessRuleException("Current user not found"));
    }

    public LeaveRequestResponse toResponse(LeaveRequest r) {
        return LeaveRequestResponse.builder()
                .id(r.getId())
                .requestedByUsername(r.getRequestedBy().getUsername())
                .reason(r.getReason())
                .startDate(r.getStartDate())
                .endDate(r.getEndDate())
                .status(r.getStatus())
                .reviewedByUsername(r.getReviewedBy() != null ? r.getReviewedBy().getUsername() : null)
                .reviewNote(r.getReviewNote())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
