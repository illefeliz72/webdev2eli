package com.elifelize.webdev2eli;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/books")
public class BookController {

    private final List<Book> bookList = new ArrayList<>();

    public BookController() {
        bookList.add(new Book(1L, "Clean Code", "Robert C. Martin"));
        bookList.add(new Book(2L, "Spring in Action", "Craig Walls"));
        bookList.add(new Book(3L, "Effective Java", "Joshua Bloch"));
    }

    @GetMapping
    @ResponseBody
    public List<Book> getBooks(@RequestParam(required = false) String author) {
        if (author != null && !author.isBlank()) {
            return bookList.stream()
                    .filter(b -> b.getAuthor().equalsIgnoreCase(author))
                    .collect(Collectors.toList());
        }
        return bookList;
    }

    @GetMapping("/view/{id}")
    public String getBookView(@PathVariable Long id, Model model) {
        Optional<Book> bookOpt = bookList.stream()
                .filter(b -> b.getId().equals(id))
                .findFirst();

        bookOpt.ifPresent(book -> model.addAttribute("book", book));
        return "book-detail";
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createBook(@ModelAttribute Book book) {
        book.setId((long) (bookList.size() + 1));
        bookList.add(book);
        return "redirect:/books";
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookList.stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}