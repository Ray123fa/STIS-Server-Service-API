package com.polstat.penyediaanserver.repository;

import com.polstat.penyediaanserver.entity.Server;
import com.polstat.penyediaanserver.entity.User;
import com.polstat.penyediaanserver.enums.ServerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {
    Page<Server> findByOwner(User owner, Pageable pageable);
    Page<Server> findByStatus(ServerStatus status, Pageable pageable);
    Page<Server> findByStatusAndOwner(ServerStatus status, User user, Pageable pageable);
}
