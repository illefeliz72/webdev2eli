package com.elifelize.webdev2eli;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/books")
public class BookController {
    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @GetMapping
    public String listBooks(Model model) {
        model.addAttribute("books", service.getAllBooks());
        return "books";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new Book());
        return "book-form";
    }

    @PostMapping("/create")
    public String createBook(@ModelAttribute("book") Book book, BindingResult result) {
        validateBook(book, result);
        if (result.hasErrors()) {
            return "book-form";
        }
        service.saveBook(book);
        return "redirect:/books";
    }

    @GetMapping("/{id}")
    public String showBookDetail(@PathVariable Long id, Model model) {
        Book book = service.getBookById(id);
        model.addAttribute("book", book);
        return "book-detail";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Book book = service.getBookById(id);
        if (book == null)
            return "redirect:/books";
        model.addAttribute("book", book);
        return "book-form";
    }

    @PostMapping("/edit/{id}")
    public String updateBook(@PathVariable Long id, @ModelAttribute("book") Book book, BindingResult result) {
        validateBook(book, result);
        if (result.hasErrors()) {
            return "book-form";
        }
        book.setId(id);
        service.saveBook(book);
        return "redirect:/books";
    }

    @PostMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        service.deleteBook(id);
        return "redirect:/books";
    }

    private void validateBook(Book book, BindingResult result) {
        if (book.getTitle() == null || book.getTitle().trim().length() < 2 || book.getTitle().trim().length() > 100) {
            result.rejectValue("title", "error.title", "Title must be between 2 and 100 characters");
        }
        if (book.getAuthor() == null || book.getAuthor().trim().length() < 2 || book.getAuthor().trim().length() > 50) {
            result.rejectValue("author", "error.author", "Author must be between 2 and 50 characters");
        }
        if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            result.rejectValue("isbn", "error.isbn", "ISBN is required");
        }
    }
}