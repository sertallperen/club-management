package com.clubmanagement.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PlayerService playerService;

    private User playerUser;
    private Player player;

    @BeforeEach
    void setUp() {
        playerUser = User.builder()
                .id(1L).username("john.doe")
                .email("john@club.com").role(RoleName.PLAYER)
                .enabled(true).build();

        player = Player.builder()
                .id(1L).user(playerUser)
                .firstName("John").lastName("Doe")
                .jerseyNumber(9).position(Position.FORWARD)
                .nationality("English")
                .contractEndDate(LocalDate.of(2027, 6, 30))
                .build();
    }

    @Test
    @DisplayName("getAll returns paginated player list")
    void getAll_returnsPaginatedList() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(playerRepository.findAllByDeletedFalse(pageable))
                .thenReturn(new PageImpl<>(List.of(player)));

        var result = playerService.getAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    @DisplayName("getById returns player when found")
    void getById_found() {
        when(playerRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(player));

        PlayerResponse response = playerService.getById(1L);

        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getPosition()).isEqualTo(Position.FORWARD);
    }

    @Test
    @DisplayName("getById throws ResourceNotFoundException when not found")
    void getById_notFound() {
        when(playerRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("create throws when user does not have PLAYER role")
    void create_wrongRole_throws() {
        User adminUser = User.builder().id(2L).role(RoleName.ADMIN).enabled(true).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        PlayerRequest request = new PlayerRequest();
        request.setUserId(2L);
        request.setFirstName("Test");
        request.setLastName("User");
        request.setPosition(Position.MIDFIELDER);

        assertThatThrownBy(() -> playerService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("PLAYER role");
    }

    @Test
    @DisplayName("create throws when jersey number already taken")
    void create_duplicateJersey_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(playerUser));
        when(playerRepository.findByUserAndDeletedFalse(playerUser)).thenReturn(Optional.empty());
        when(playerRepository.existsByJerseyNumberAndDeletedFalse(9)).thenReturn(true);

        PlayerRequest request = new PlayerRequest();
        request.setUserId(1L);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPosition(Position.FORWARD);
        request.setJerseyNumber(9);

        assertThatThrownBy(() -> playerService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Jersey number");
    }

    @Test
    @DisplayName("create succeeds with valid data")
    void create_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(playerUser));
        when(playerRepository.findByUserAndDeletedFalse(playerUser)).thenReturn(Optional.empty());
        when(playerRepository.existsByJerseyNumberAndDeletedFalse(9)).thenReturn(false);
        when(playerRepository.save(any(Player.class))).thenReturn(player);

        PlayerRequest request = new PlayerRequest();
        request.setUserId(1L);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPosition(Position.FORWARD);
        request.setJerseyNumber(9);

        PlayerResponse response = playerService.create(request);

        assertThat(response.getFirstName()).isEqualTo("John");
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    @DisplayName("softDelete marks player as deleted")
    void softDelete_success() {
        when(playerRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenReturn(player);

        playerService.softDelete(1L);

        assertThat(player.isDeleted()).isTrue();
        verify(playerRepository).save(player);
    }
}
