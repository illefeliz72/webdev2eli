package main.java.com.elifelize.webdev2eli.repository;

import com.elifelize.webdev2eli.model.Task;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PetRepository {

    private final List<Pet> tasks = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public PetRepository() {
        save(new Pet(null, "Setup Spring Boot", "Initialize project with Web, Thymeleaf, Validation", true));
        save(new Pet(null, "Create Task Controller", "Implement list, detail, and form endpoints", false));
    }

    public List<Pet> findAll() {
        return new ArrayList<>(tasks);
    }

    public Optional<Pet> findById(Long id) {
        return tasks.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();
    }

    public Pet save(Pet task) {
        if (task.getId() == null) {
            task.setId(idCounter.getAndIncrement());
        }
        tasks.add(task);
        return task;
    }
}