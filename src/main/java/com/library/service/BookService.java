package com.library.service;
import com.library.model.LibraryStatistics;
import java.util.ArrayList;
import java.util.Locale;
import com.library.model.Book;
import com.library.model.BookStatus;
import com.library.repository.BookRepository;
import com.library.util.InputValidator;

import java.io.IOException;
import java.util.List;
/**
 * Manages book operations including creation, retrieval,
 * updating, deletion, searching and filtering.
 *
 * @author Ibrahim Muhammad Abbas
 * @version 1.0
 */
public class BookService {
    public LibraryStatistics getStatistics()
        throws IOException, InterruptedException {

    List<Book> books = getAllBooks();

    int available = 0;
    int issued = 0;
    int reserved = 0;

    for (Book book : books) {

        switch (book.getStatus()) {

            case AVAILABLE:
                available++;
                break;

            case ISSUED:
                issued++;
                break;

            case RESERVED:
                reserved++;
                break;
        }
    }

    return new LibraryStatistics(
            books.size(), available, issued, reserved
    );
}
    public List<Book> filterBooks(BookStatus status)
        throws IOException, InterruptedException {

    InputValidator.validateStatus(status);

    List<Book> books = getAllBooks();
    List<Book> results = new ArrayList<>();

    for (Book book : books) {

        if (book.getStatus() == status) {
            results.add(book);
        }
    }

    return results;
}
    public List<Book> searchBooks(String keyword)
        throws IOException, InterruptedException {

    List<Book> books = getAllBooks();
    List<Book> results = new ArrayList<>();

    if (keyword == null || keyword.isBlank()) {
        return books;
    }

    keyword = keyword.trim().toLowerCase(Locale.ROOT);

    for (Book book : books) {

        if (String.valueOf(book.getId()).equals(keyword)
                || book.getTitle().toLowerCase(Locale.ROOT).contains(keyword)
                || book.getAuthor().toLowerCase(Locale.ROOT).contains(keyword)) {

            results.add(book);
        }
    }

    return results;
}

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    /**
 * Adds a new book after validating its details.
 *
 * @param id unique book ID
 * @param title title of the book
 * @param author author of the book
 * @return the newly created book
 * @throws IllegalArgumentException if input is invalid or ID exists
 * @throws IOException if communication with Supabase fails
 * @throws InterruptedException if the request is interrupted
 */
    public Book addBook(int id, String title, String author)
            throws IOException, InterruptedException {

        InputValidator.validateId(id);
        InputValidator.validateDetails(title, author);

        if (repository.findById(id) != null) {
            throw new IllegalArgumentException(
                    "Book ID already exists."
            );
        }

        Book book = new Book(id, title.trim(), author.trim());

        return repository.add(book);
    }

    public List<Book> getAllBooks()
            throws IOException, InterruptedException {

        return repository.findAll();
    }

    public Book getBookById(int id)
            throws IOException, InterruptedException {

        InputValidator.validateId(id);

        Book book = repository.findById(id);

        if (book == null) {
            throw new IllegalArgumentException(
                    "Book not found."
            );
        }

        return book;
    }

    public Book updateBook(int id, String title, String author)
            throws IOException, InterruptedException {

        InputValidator.validateId(id);
        InputValidator.validateDetails(title, author);

        return repository.update(
                id, title.trim(), author.trim()
        );
    }

    public Book changeStatus(int id, BookStatus status)
            throws IOException, InterruptedException {

        InputValidator.validateId(id);
        InputValidator.validateStatus(status);

        return repository.updateStatus(id, status);
    }

    public void deleteBook(int id)
            throws IOException, InterruptedException {

        InputValidator.validateId(id);

        repository.delete(id);
    }
}