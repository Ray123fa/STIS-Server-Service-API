package com.polstat.penyediaanserver.repository;

import com.polstat.penyediaanserver.entity.Server;
import com.polstat.penyediaanserver.entity.User;
import com.polstat.penyediaanserver.enums.ServerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {
    List<Server> findByOwner(User owner);
    Page<Server> findByOwner(User owner, Pageable pageable);
    Page<Server> findByStatus(ServerStatus status, Pageable pageable);
    List<Server> findByOwnerAndStatus(User owner, ServerStatus status);
}
