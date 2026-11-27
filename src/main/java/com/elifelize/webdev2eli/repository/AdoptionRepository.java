package com.elifelize.webdev2eli.repository;

import com.elifelize.webdev2eli.entity.Adoption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdoptionRepository extends JpaRepository<Adoption, Long> {
}