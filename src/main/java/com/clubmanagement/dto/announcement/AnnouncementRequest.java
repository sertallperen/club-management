package com.clubmanagement.dto.announcement;

import com.clubmanagement.enums.RoleName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnouncementRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "Content is required")
    @Size(max = 2000)
    private String content;

    private RoleName targetRole;

    private boolean pinned = false;
}
