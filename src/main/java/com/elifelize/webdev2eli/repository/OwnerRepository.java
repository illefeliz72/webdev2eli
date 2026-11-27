package com.elifelize.webdev2eli.repository;

import com.elifelize.webdev2eli.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
    boolean existsByEmailIgnoreCase(String email);
}