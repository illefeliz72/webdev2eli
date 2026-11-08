package com.elifelize.webdev2eli.runner;

import com.elifelize.webdev2eli.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class EntityLifecycleRunner implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        User user = new User("johndoe", "john.original@example.com", LocalDateTime.now());

        entityManager.persist(user);
        entityManager.flush();

        entityManager.detach(user);

        user.setEmail("john.modified@example.com");

        entityManager.flush();
    }
}