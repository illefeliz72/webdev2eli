package com.elifelize.webdev2eli;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookService {
    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> getAllBooks() {
        return repository.findAll();
    }

    public Book getBookById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void saveBook(Book book) {
        repository.save(book);
    }

    public void deleteBook(Long id) {
        repository.deleteById(id);
    }
}