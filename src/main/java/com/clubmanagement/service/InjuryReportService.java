package com.clubmanagement.service;

import com.clubmanagement.dto.common.PagedResponse;
import com.clubmanagement.dto.injury.InjuryReportRequest;
import com.clubmanagement.dto.injury.InjuryReportResponse;
import com.clubmanagement.entity.InjuryReport;
import com.clubmanagement.entity.Player;
import com.clubmanagement.entity.User;
import com.clubmanagement.enums.InjuryStatus;
import com.clubmanagement.enums.RoleName;
import com.clubmanagement.exception.BusinessRuleException;
import com.clubmanagement.exception.ResourceNotFoundException;
import com.clubmanagement.repository.InjuryReportRepository;
import com.clubmanagement.repository.PlayerRepository;
import com.clubmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InjuryReportService {

    private final InjuryReportRepository injuryRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    public PagedResponse<InjuryReportResponse> getAll(Pageable pageable) {
        User current = getCurrentUser();
        if (current.getRole() == RoleName.PLAYER) {
            Player player = playerRepository.findByUserAndDeletedFalse(current)
                    .orElseThrow(() -> new BusinessRuleException("Player profile not found"));
            List<InjuryReportResponse> list = injuryRepository
                    .findByPlayerAndDeletedFalseOrderByInjuryDateDesc(player)
                    .stream().map(this::toResponse).toList();
            return PagedResponse.<InjuryReportResponse>builder()
                    .content(list).page(0).size(list.size())
                    .totalElements(list.size()).totalPages(1).last(true).build();
        }
        return PagedResponse.of(injuryRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    public InjuryReportResponse getById(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public InjuryReportResponse create(InjuryReportRequest request) {
        Player player = playerRepository.findByIdAndDeletedFalse(request.getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player", request.getPlayerId()));
        User reporter = getCurrentUser();

        InjuryReport report = InjuryReport.builder()
                .player(player)
                .reportedBy(reporter)
                .injuryType(request.getInjuryType())
                .bodyPart(request.getBodyPart())
                .description(request.getDescription())
                .injuryDate(request.getInjuryDate())
                .expectedReturnDate(request.getExpectedReturnDate())
                .status(InjuryStatus.ACTIVE)
                .build();

        return toResponse(injuryRepository.save(report));
    }

    @Transactional
    public InjuryReportResponse updateStatus(Long id, InjuryStatus newStatus) {
        InjuryReport report = findActive(id);
        report.setStatus(newStatus);
        return toResponse(injuryRepository.save(report));
    }

    @Transactional
    public void softDelete(Long id) {
        InjuryReport report = findActive(id);
        report.setDeleted(true);
        injuryRepository.save(report);
    }

    private InjuryReport findActive(Long id) {
        return injuryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("InjuryReport", id));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new BusinessRuleException("Current user not found"));
    }

    public InjuryReportResponse toResponse(InjuryReport r) {
        Player p = r.getPlayer();
        return InjuryReportResponse.builder()
                .id(r.getId())
                .playerId(p.getId())
                .playerFullName(p.getFirstName() + " " + p.getLastName())
                .reportedByUsername(r.getReportedBy() != null ? r.getReportedBy().getUsername() : null)
                .injuryType(r.getInjuryType())
                .bodyPart(r.getBodyPart())
                .description(r.getDescription())
                .injuryDate(r.getInjuryDate())
                .expectedReturnDate(r.getExpectedReturnDate())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
