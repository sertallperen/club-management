package com.clubmanagement.repository;

import com.clubmanagement.entity.LeaveRequest;
import com.clubmanagement.entity.User;
import com.clubmanagement.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    Optional<LeaveRequest> findByIdAndDeletedFalse(Long id);

    Page<LeaveRequest> findAllByDeletedFalse(Pageable pageable);

    List<LeaveRequest> findByRequestedByAndDeletedFalseOrderByCreatedAtDesc(User user);

    List<LeaveRequest> findByStatusAndDeletedFalse(RequestStatus status);
}
