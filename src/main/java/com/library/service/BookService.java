package com.library.service;

import com.library.model.Book;
import com.library.model.BookStatus;
import com.library.repository.BookRepository;
import com.library.util.InputValidator;

import java.io.IOException;
import java.util.List;

public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

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