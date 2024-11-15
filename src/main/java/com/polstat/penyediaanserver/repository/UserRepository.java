package com.polstat.penyediaanserver.repository;

import com.polstat.penyediaanserver.entity.User;
import com.polstat.penyediaanserver.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findAllByRole(Role role, Pageable pageable);
}

