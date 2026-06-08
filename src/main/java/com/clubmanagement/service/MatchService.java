package com.clubmanagement.service;

import com.clubmanagement.dto.common.PagedResponse;
import com.clubmanagement.dto.match.MatchRequest;
import com.clubmanagement.dto.match.MatchResponse;
import com.clubmanagement.entity.ClubMatch;
import com.clubmanagement.enums.MatchStatus;
import com.clubmanagement.enums.VenueType;
import com.clubmanagement.exception.ResourceNotFoundException;
import com.clubmanagement.repository.ClubMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final ClubMatchRepository matchRepository;

    public PagedResponse<MatchResponse> getAll(Pageable pageable) {
        return PagedResponse.of(matchRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    public PagedResponse<MatchResponse> search(MatchStatus status, LocalDateTime from,
                                                LocalDateTime to, String opponent, Pageable pageable) {
        return PagedResponse.of(
                matchRepository.searchMatches(status, from, to, opponent, pageable).map(this::toResponse));
    }

    public List<MatchResponse> getUpcoming() {
        return matchRepository
                .findByStatusAndDeletedFalseAndMatchDateAfterOrderByMatchDateAsc(
                        MatchStatus.SCHEDULED, LocalDateTime.now())
                .stream().map(this::toResponse).toList();
    }

    public MatchResponse getById(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public MatchResponse create(MatchRequest request) {
        ClubMatch match = ClubMatch.builder()
                .opponent(request.getOpponent())
                .matchDate(request.getMatchDate())
                .venue(request.getVenue())
                .venueType(request.getVenueType())
                .competition(request.getCompetition())
                .status(request.getStatus() != null ? request.getStatus() : MatchStatus.SCHEDULED)
                .homeScore(request.getHomeScore())
                .awayScore(request.getAwayScore())
                .notes(request.getNotes())
                .broadcastChannel(request.getBroadcastChannel())
                .build();
        return toResponse(matchRepository.save(match));
    }

    @Transactional
    public MatchResponse update(Long id, MatchRequest request) {
        ClubMatch match = findActive(id);
        match.setOpponent(request.getOpponent());
        match.setMatchDate(request.getMatchDate());
        match.setVenue(request.getVenue());
        match.setVenueType(request.getVenueType());
        match.setCompetition(request.getCompetition());
        if (request.getStatus() != null) match.setStatus(request.getStatus());
        match.setHomeScore(request.getHomeScore());
        match.setAwayScore(request.getAwayScore());
        match.setNotes(request.getNotes());
        match.setBroadcastChannel(request.getBroadcastChannel());
        return toResponse(matchRepository.save(match));
    }

    @Transactional
    public void softDelete(Long id) {
        ClubMatch match = findActive(id);
        match.setDeleted(true);
        matchRepository.save(match);
    }

    // CSV export for match history
    public String exportToCsv() {
        List<ClubMatch> matches = matchRepository.findAllByDeletedFalse(Pageable.unpaged()).getContent();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Opponent,Date,Venue,Type,Competition,Status,Home Score,Away Score\n");
        for (ClubMatch m : matches) {
            csv.append(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    m.getId(), m.getOpponent(), m.getMatchDate(),
                    m.getVenue() != null ? m.getVenue() : "",
                    m.getVenueType(), m.getCompetition() != null ? m.getCompetition() : "",
                    m.getStatus(),
                    m.getHomeScore() != null ? m.getHomeScore() : "",
                    m.getAwayScore() != null ? m.getAwayScore() : ""));
        }
        return csv.toString();
    }

    private ClubMatch findActive(Long id) {
        return matchRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match", id));
    }

    public MatchResponse toResponse(ClubMatch m) {
        return MatchResponse.builder()
                .id(m.getId())
                .opponent(m.getOpponent())
                .matchDate(m.getMatchDate())
                .venue(m.getVenue())
                .venueType(m.getVenueType())
                .competition(m.getCompetition())
                .status(m.getStatus())
                .homeScore(m.getHomeScore())
                .awayScore(m.getAwayScore())
                .notes(m.getNotes())
                .broadcastChannel(m.getBroadcastChannel())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
