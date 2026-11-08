package com.elifelize.webdev2eli.repository;

import com.elifelize.webdev2eli.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}