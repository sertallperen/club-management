package com.clubmanagement.service;

import com.clubmanagement.dto.announcement.AnnouncementResponse;
import com.clubmanagement.dto.match.MatchResponse;
import com.clubmanagement.dto.meal.DailyMealResponse;
import com.clubmanagement.dto.task.PlayerTaskResponse;
import com.clubmanagement.dto.training.TrainingSessionResponse;
import com.clubmanagement.entity.Player;
import com.clubmanagement.entity.User;
import com.clubmanagement.enums.RoleName;
import com.clubmanagement.enums.TaskStatus;
import com.clubmanagement.exception.BusinessRuleException;
import com.clubmanagement.repository.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final ClubMatchRepository matchRepository;
    private final TrainingSessionRepository trainingRepository;
    private final DailyMealRepository mealRepository;
    private final AnnouncementRepository announcementRepository;
    private final PlayerTaskRepository taskRepository;

    private final MatchService matchService;
    private final TrainingSessionService trainingService;
    private final DailyMealService mealService;
    private final AnnouncementService announcementService;
    private final PlayerTaskService taskService;

    public DashboardData getDashboard() {
        User current = getCurrentUser();

        List<TrainingSessionResponse> todayTraining = trainingRepository
                .findBySessionDateAndDeletedFalseOrderByStartTimeAsc(LocalDate.now())
                .stream().map(trainingService::toResponse).toList();

        List<DailyMealResponse> todayMeals = mealRepository
                .findByMealDateAndDeletedFalseOrderByMealTypeAsc(LocalDate.now())
                .stream().map(mealService::toResponse).toList();

        List<AnnouncementResponse> announcements = announcementRepository
                .findVisibleForRole(current.getRole())
                .stream().limit(5).map(announcementService::toResponse).toList();

        List<MatchResponse> upcomingMatches = matchService.getUpcoming()
                .stream().limit(3).toList();

        List<PlayerTaskResponse> myTasks = List.of();
        if (current.getRole() == RoleName.PLAYER) {
            Player player = playerRepository.findByUserAndDeletedFalse(current).orElse(null);
            if (player != null) {
                myTasks = taskRepository.findByPlayerAndStatusAndDeletedFalse(player, TaskStatus.PENDING)
                        .stream().map(taskService::toResponse).toList();
            }
        }

        return DashboardData.builder()
                .username(current.getUsername())
                .role(current.getRole())
                .todayDate(LocalDate.now())
                .todayTrainingSessions(todayTraining)
                .todayMeals(todayMeals)
                .recentAnnouncements(announcements)
                .upcomingMatches(upcomingMatches)
                .pendingTasks(myTasks)
                .build();
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new BusinessRuleException("Current user not found"));
    }

    @Getter
    @Builder
    public static class DashboardData {
        private String username;
        private RoleName role;
        private LocalDate todayDate;
        private List<TrainingSessionResponse> todayTrainingSessions;
        private List<DailyMealResponse> todayMeals;
        private List<AnnouncementResponse> recentAnnouncements;
        private List<MatchResponse> upcomingMatches;
        private List<PlayerTaskResponse> pendingTasks;
    }
}
