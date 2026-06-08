package com.clubmanagement.service;

import com.clubmanagement.dto.common.PagedResponse;
import com.clubmanagement.dto.training.TrainingSessionRequest;
import com.clubmanagement.dto.training.TrainingSessionResponse;
import com.clubmanagement.entity.TrainingSession;
import com.clubmanagement.entity.User;
import com.clubmanagement.exception.ResourceNotFoundException;
import com.clubmanagement.repository.TrainingSessionRepository;
import com.clubmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingSessionService {

    private final TrainingSessionRepository trainingRepo;
    private final UserRepository userRepository;

    public PagedResponse<TrainingSessionResponse> getAll(Pageable pageable) {
        return PagedResponse.of(trainingRepo.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    public List<TrainingSessionResponse> getToday() {
        return trainingRepo.findBySessionDateAndDeletedFalseOrderByStartTimeAsc(LocalDate.now())
                .stream().map(this::toResponse).toList();
    }

    public List<TrainingSessionResponse> getWeek() {
        LocalDate today = LocalDate.now();
        return trainingRepo.findBySessionDateBetweenAndDeletedFalseOrderBySessionDateAsc(
                today, today.plusDays(7)).stream().map(this::toResponse).toList();
    }

    public TrainingSessionResponse getById(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public TrainingSessionResponse create(TrainingSessionRequest request) {
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new com.clubmanagement.exception.BusinessRuleException("End time must be after start time");
        }

        User currentUser = getCurrentUser();

        TrainingSession session = TrainingSession.builder()
                .sessionDate(request.getSessionDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .focus(request.getFocus())
                .intensity(request.getIntensity())
                .notes(request.getNotes())
                .createdBy(currentUser)
                .build();

        return toResponse(trainingRepo.save(session));
    }

    @Transactional
    public TrainingSessionResponse update(Long id, TrainingSessionRequest request) {
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new com.clubmanagement.exception.BusinessRuleException("End time must be after start time");
        }
        TrainingSession session = findActive(id);
        session.setSessionDate(request.getSessionDate());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setLocation(request.getLocation());
        session.setFocus(request.getFocus());
        session.setIntensity(request.getIntensity());
        session.setNotes(request.getNotes());
        return toResponse(trainingRepo.save(session));
    }

    @Transactional
    public void softDelete(Long id) {
        TrainingSession session = findActive(id);
        session.setDeleted(true);
        trainingRepo.save(session);
    }

    private TrainingSession findActive(Long id) {
        return trainingRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingSession", id));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsernameAndDeletedFalse(username).orElse(null);
    }

    public TrainingSessionResponse toResponse(TrainingSession s) {
        return TrainingSessionResponse.builder()
                .id(s.getId())
                .sessionDate(s.getSessionDate())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .location(s.getLocation())
                .focus(s.getFocus())
                .intensity(s.getIntensity())
                .notes(s.getNotes())
                .createdByUsername(s.getCreatedBy() != null ? s.getCreatedBy().getUsername() : null)
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
