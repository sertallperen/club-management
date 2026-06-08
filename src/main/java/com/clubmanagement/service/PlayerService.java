package com.clubmanagement.service;

import com.clubmanagement.dto.common.PagedResponse;
import com.clubmanagement.dto.player.ConditionUpdateRequest;
import com.clubmanagement.dto.player.PlayerRequest;
import com.clubmanagement.dto.player.PlayerResponse;
import com.clubmanagement.entity.Player;
import com.clubmanagement.entity.User;
import com.clubmanagement.enums.Position;
import com.clubmanagement.enums.RoleName;
import com.clubmanagement.exception.BusinessRuleException;
import com.clubmanagement.exception.ResourceNotFoundException;
import com.clubmanagement.repository.PlayerRepository;
import com.clubmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    public PagedResponse<PlayerResponse> getAll(Pageable pageable) {
        return PagedResponse.of(playerRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    public PagedResponse<PlayerResponse> search(Position position, String nationality,
                                                 String name, Pageable pageable) {
        return PagedResponse.of(
                playerRepository.searchPlayers(position, nationality, name, pageable).map(this::toResponse));
    }

    public PlayerResponse getById(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public PlayerResponse create(PlayerRequest request) {
        User user = userRepository.findById(request.getUserId())
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        if (user.getRole() != RoleName.PLAYER) {
            throw new BusinessRuleException("User must have PLAYER role to create a player profile");
        }

        if (playerRepository.findByUserAndDeletedFalse(user).isPresent()) {
            throw new BusinessRuleException("This user already has a player profile");
        }

        if (request.getJerseyNumber() != null &&
                playerRepository.existsByJerseyNumberAndDeletedFalse(request.getJerseyNumber())) {
            throw new BusinessRuleException("Jersey number " + request.getJerseyNumber() + " is already taken");
        }

        Player player = Player.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .jerseyNumber(request.getJerseyNumber())
                .position(request.getPosition())
                .nationality(request.getNationality())
                .dateOfBirth(request.getDateOfBirth())
                .weightKg(request.getWeightKg())
                .heightCm(request.getHeightCm())
                .marketValue(request.getMarketValue())
                .contractEndDate(request.getContractEndDate())
                .build();

        return toResponse(playerRepository.save(player));
    }

    @Transactional
    public PlayerResponse update(Long id, PlayerRequest request) {
        Player player = findActive(id);

        if (request.getJerseyNumber() != null &&
                !request.getJerseyNumber().equals(player.getJerseyNumber()) &&
                playerRepository.existsByJerseyNumberAndDeletedFalse(request.getJerseyNumber())) {
            throw new BusinessRuleException("Jersey number " + request.getJerseyNumber() + " is already taken");
        }

        player.setFirstName(request.getFirstName());
        player.setLastName(request.getLastName());
        player.setJerseyNumber(request.getJerseyNumber());
        player.setPosition(request.getPosition());
        player.setNationality(request.getNationality());
        player.setDateOfBirth(request.getDateOfBirth());
        player.setWeightKg(request.getWeightKg());
        player.setHeightCm(request.getHeightCm());
        player.setMarketValue(request.getMarketValue());
        player.setContractEndDate(request.getContractEndDate());

        return toResponse(playerRepository.save(player));
    }

    @Transactional
    public PlayerResponse updateCondition(Long id, ConditionUpdateRequest request) {
        Player player = findActive(id);
        player.setConditionRating(request.getConditionRating());
        player.setConditionNotes(request.getConditionNotes());
        return toResponse(playerRepository.save(player));
    }

    @Transactional
    public void softDelete(Long id) {
        Player player = findActive(id);
        player.setDeleted(true);
        playerRepository.save(player);
    }

    // CSV Export
    public String exportToCsv() {
        List<Player> players = playerRepository.findAllByDeletedFalse(Pageable.unpaged()).getContent();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,First Name,Last Name,Jersey Number,Position,Nationality,Date of Birth,Weight (kg),Height (cm),Contract End\n");
        for (Player p : players) {
            csv.append(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    p.getId(),
                    p.getFirstName(),
                    p.getLastName(),
                    p.getJerseyNumber() != null ? p.getJerseyNumber() : "",
                    p.getPosition(),
                    p.getNationality() != null ? p.getNationality() : "",
                    p.getDateOfBirth() != null ? p.getDateOfBirth() : "",
                    p.getWeightKg() != null ? p.getWeightKg() : "",
                    p.getHeightCm() != null ? p.getHeightCm() : "",
                    p.getContractEndDate() != null ? p.getContractEndDate() : ""
            ));
        }
        return csv.toString();
    }

    private Player findActive(Long id) {
        return playerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", id));
    }

    public PlayerResponse toResponse(Player p) {
        return PlayerResponse.builder()
                .id(p.getId())
                .userId(p.getUser().getId())
                .username(p.getUser().getUsername())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .fullName(p.getFirstName() + " " + p.getLastName())
                .jerseyNumber(p.getJerseyNumber())
                .position(p.getPosition())
                .nationality(p.getNationality())
                .dateOfBirth(p.getDateOfBirth())
                .weightKg(p.getWeightKg())
                .heightCm(p.getHeightCm())
                .marketValue(p.getMarketValue())
                .contractEndDate(p.getContractEndDate())
                .conditionRating(p.getConditionRating())
                .conditionNotes(p.getConditionNotes())
                .photoUrl(p.getPhotoUrl())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
