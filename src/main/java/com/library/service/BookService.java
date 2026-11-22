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
    /**
     * Counts each status in the current fetched collection.
     * @return the total, available, issued and reserved counts
     * @throws IOException if the HTTP request fails or Supabase rejects it
     * @throws InterruptedException if the request is interrupted
     */
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
    /**
     * Fetches all books and selects those with the requested status.
     * @param status book status
     * @return matching books in repository order
     * @throws IOException if the HTTP request fails or Supabase rejects it
     * @throws InterruptedException if the request is interrupted
     */
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
    /**
     * Fetches books and matches an exact numeric ID or a case-insensitive title or author substring.
     * @param keyword ID or title or author search text
     * @return matching books, or all books for a blank keyword
     * @throws IOException if the HTTP request fails or Supabase rejects it
     * @throws InterruptedException if the request is interrupted
     */
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

    /**
     * Creates a BookService instance using the supplied values and collaborators.
     * @param repository book persistence adapter
     */
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

    /**
     * Retrieves all books through the repository.
     * @return books ordered by ascending ID
     * @throws IOException if the HTTP request fails or Supabase rejects it
     * @throws InterruptedException if the request is interrupted
     */
    public List<Book> getAllBooks()
            throws IOException, InterruptedException {

        return repository.findAll();
    }

    /**
     * Validates the ID and retrieves the book, rejecting a missing record.
     * @param id positive book ID
     * @return the matching book
     * @throws IOException if the HTTP request fails or Supabase rejects it
     * @throws InterruptedException if the request is interrupted
     * @throws IllegalArgumentException if the ID is invalid or no book exists
     */
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

    /**
     * Validates and trims title and author before updating the selected book.
     * @param id positive book ID
     * @param title book title
     * @param author book author
     * @return the updated book
     * @throws IOException if the HTTP request fails or Supabase rejects it
     * @throws InterruptedException if the request is interrupted
     */
    public Book updateBook(int id, String title, String author)
            throws IOException, InterruptedException {

        InputValidator.validateId(id);
        InputValidator.validateDetails(title, author);

        return repository.update(
                id, title.trim(), author.trim()
        );
    }

    /**
     * Validates the ID and status before persisting the status change.
     * @param id positive book ID
     * @param status book status
     * @return the updated book
     * @throws IOException if the HTTP request fails or Supabase rejects it
     * @throws InterruptedException if the request is interrupted
     */
    public Book changeStatus(int id, BookStatus status)
            throws IOException, InterruptedException {

        InputValidator.validateId(id);
        InputValidator.validateStatus(status);

        return repository.updateStatus(id, status);
    }

    /**
     * Validates the ID and deletes the selected book.
     * @param id positive book ID
     * @throws IOException if the HTTP request fails or Supabase rejects it
     * @throws InterruptedException if the request is interrupted
     */
    public void deleteBook(int id)
            throws IOException, InterruptedException {

        InputValidator.validateId(id);

        repository.delete(id);
    }
}