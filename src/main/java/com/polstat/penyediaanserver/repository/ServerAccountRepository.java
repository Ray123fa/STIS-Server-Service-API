package com.polstat.penyediaanserver.repository;

import com.polstat.penyediaanserver.entity.ServerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerAccountRepository extends JpaRepository<ServerAccount, Long> {
    boolean existsByUsername(String username);
}