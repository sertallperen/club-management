package com.clubmanagement.entity;

import com.clubmanagement.enums.InjuryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "injury_reports", indexes = {
    @Index(name = "idx_injury_player", columnList = "player_id"),
    @Index(name = "idx_injury_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InjuryReport extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_id")
    private User reportedBy;

    @Column(nullable = false, length = 100)
    private String injuryType;

    @Column(length = 80)
    private String bodyPart;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDate injuryDate;

    private LocalDate expectedReturnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InjuryStatus status = InjuryStatus.ACTIVE;
}
