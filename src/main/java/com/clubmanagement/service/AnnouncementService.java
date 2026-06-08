package com.clubmanagement.service;

import com.clubmanagement.dto.announcement.AnnouncementRequest;
import com.clubmanagement.dto.announcement.AnnouncementResponse;
import com.clubmanagement.dto.common.PagedResponse;
import com.clubmanagement.entity.Announcement;
import com.clubmanagement.entity.User;
import com.clubmanagement.exception.BusinessRuleException;
import com.clubmanagement.exception.ResourceNotFoundException;
import com.clubmanagement.repository.AnnouncementRepository;
import com.clubmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    public PagedResponse<AnnouncementResponse> getAll(Pageable pageable) {
        return PagedResponse.of(announcementRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    public List<AnnouncementResponse> getForCurrentUser() {
        User current = getCurrentUser();
        return announcementRepository.findVisibleForRole(current.getRole())
                .stream().map(this::toResponse).toList();
    }

    public AnnouncementResponse getById(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public AnnouncementResponse create(AnnouncementRequest request) {
        User creator = getCurrentUser();
        Announcement announcement = Announcement.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .targetRole(request.getTargetRole())
                .pinned(request.isPinned())
                .createdBy(creator)
                .build();
        return toResponse(announcementRepository.save(announcement));
    }

    @Transactional
    public AnnouncementResponse update(Long id, AnnouncementRequest request) {
        Announcement announcement = findActive(id);
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setTargetRole(request.getTargetRole());
        announcement.setPinned(request.isPinned());
        return toResponse(announcementRepository.save(announcement));
    }

    @Transactional
    public void softDelete(Long id) {
        Announcement announcement = findActive(id);
        announcement.setDeleted(true);
        announcementRepository.save(announcement);
    }

    private Announcement findActive(Long id) {
        return announcementRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", id));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new BusinessRuleException("Current user not found"));
    }

    public AnnouncementResponse toResponse(Announcement a) {
        return AnnouncementResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .content(a.getContent())
                .targetRole(a.getTargetRole())
                .pinned(a.isPinned())
                .createdByUsername(a.getCreatedBy().getUsername())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
