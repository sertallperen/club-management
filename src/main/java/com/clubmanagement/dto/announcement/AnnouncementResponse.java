package com.clubmanagement.dto.announcement;

import com.clubmanagement.enums.RoleName;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String content;
    private RoleName targetRole;
    private boolean pinned;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
