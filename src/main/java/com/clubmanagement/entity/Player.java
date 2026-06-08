package com.clubmanagement.entity;

import com.clubmanagement.enums.Position;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(unique = true)
    private Integer jerseyNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Position position;

    @Column(length = 50)
    private String nationality;

    private LocalDate dateOfBirth;

    private Double weightKg;

    private Double heightCm;

    @Column(length = 100)
    private String marketValue;

    private LocalDate contractEndDate;

    // Self-reported condition (1-5 scale)
    private Integer conditionRating;

    @Column(length = 255)
    private String conditionNotes;

    @Column(length = 500)
    private String photoUrl;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PlayerTask> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InjuryReport> injuryReports = new ArrayList<>();

}
