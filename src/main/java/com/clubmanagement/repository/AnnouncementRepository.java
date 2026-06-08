package com.clubmanagement.repository;

import com.clubmanagement.entity.Announcement;
import com.clubmanagement.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    Optional<Announcement> findByIdAndDeletedFalse(Long id);

    Page<Announcement> findAllByDeletedFalse(Pageable pageable);

    // Returns announcements targeting this role OR broadcast to all (targetRole IS NULL)
    @Query("SELECT a FROM Announcement a WHERE a.deleted = false " +
           "AND (a.targetRole IS NULL OR a.targetRole = :role) " +
           "ORDER BY a.pinned DESC, a.createdAt DESC")
    List<Announcement> findVisibleForRole(@Param("role") RoleName role);
}
