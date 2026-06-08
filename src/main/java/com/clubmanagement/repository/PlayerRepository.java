package com.clubmanagement.repository;

import com.clubmanagement.entity.Player;
import com.clubmanagement.entity.User;
import com.clubmanagement.enums.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByIdAndDeletedFalse(Long id);

    Optional<Player> findByUserAndDeletedFalse(User user);

    Page<Player> findAllByDeletedFalse(Pageable pageable);

    boolean existsByJerseyNumberAndDeletedFalse(Integer jerseyNumber);

    // Advanced search: filter by position, nationality, name
    @Query("SELECT p FROM Player p WHERE p.deleted = false " +
           "AND (:position IS NULL OR p.position = :position) " +
           "AND (:nationality IS NULL OR LOWER(p.nationality) LIKE LOWER(CONCAT('%', :nationality, '%'))) " +
           "AND (:name IS NULL OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "     OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Player> searchPlayers(@Param("position") Position position,
                               @Param("nationality") String nationality,
                               @Param("name") String name,
                               Pageable pageable);
}
