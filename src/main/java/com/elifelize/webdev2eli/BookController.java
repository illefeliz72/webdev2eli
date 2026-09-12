package com.elifelize.webdev2eli;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {

    private final List<Book> bookList = new ArrayList<>();

    public BookController() {
        bookList.add(new Book(1L, "Spring Boot in Action", "Craig Walls", 39.99));
        bookList.add(new Book(2L, "Clean Code", "Robert C. Martin", 42.50));
    }

    @GetMapping
    public String getAllBooks(Model model) {
        model.addAttribute("books", bookList);
        return "books";
    }

    @GetMapping("/{id}")
    public String getBookById(@PathVariable Long id, Model model) {
        Book book = bookList.stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElse(null);
        model.addAttribute("book", book);
        return "book-detail";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new Book());
        return "book-form";
    }

    @PostMapping
    public String saveBook(@ModelAttribute("book") Book book, Model model) {
        if (book.getTitle() == null || book.getTitle().isBlank() ||
                book.getAuthor() == null || book.getAuthor().isBlank() ||
                book.getPrice() == null || book.getPrice() < 0) {

            model.addAttribute("errorMessage", "All fields are required and price must be positive.");
            return "book-form";
        }

        book.setId((long) (bookList.size() + 1));
        bookList.add(book);
        return "redirect:/books";
    }
}