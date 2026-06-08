package com.clubmanagement.entity;

import com.clubmanagement.enums.MatchStatus;
import com.clubmanagement.enums.VenueType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "club_matches", indexes = {
    @Index(name = "idx_match_date", columnList = "matchDate"),
    @Index(name = "idx_match_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubMatch extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String opponent;

    @Column(nullable = false)
    private LocalDateTime matchDate;

    @Column(length = 100)
    private String venue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private VenueType venueType;

    @Column(length = 80)
    private String competition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private MatchStatus status = MatchStatus.SCHEDULED;

    private Integer homeScore;
    private Integer awayScore;

    @Column(length = 500)
    private String notes;

    @Column(length = 200)
    private String broadcastChannel;
}
