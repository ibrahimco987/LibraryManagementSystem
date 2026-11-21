package com.library.service;

import com.library.model.Book;
import com.library.model.BookStatus;
import com.library.model.LibraryStatistics;
import com.library.repository.BookRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookServiceTest {

    private BookService service;

    @BeforeEach
    void setUp() {
        service = new BookService(new TestBookRepository());
    }

    @Test
    void testAddBook() throws Exception {

        Book book = service.addBook(101, "Clean Code", "Robert Martin");

        assertEquals(101, book.getId());
        assertEquals("Clean Code", book.getTitle());
        assertEquals(BookStatus.AVAILABLE, book.getStatus());
    }

    @Test
    void testDuplicateId() throws Exception {

        service.addBook(101, "Clean Code", "Robert Martin");

        assertThrows(IllegalArgumentException.class,
                () -> service.addBook(101, "Another Book", "Author"));
    }

    @Test
    void testUpdateBook() throws Exception {

        service.addBook(101, "Old Title", "Author");

        Book updated = service.updateBook(101, "New Title", "Author");

        assertEquals("New Title", updated.getTitle());
    }

    @Test
    void testDeleteBook() throws Exception {

        service.addBook(101, "Clean Code", "Robert Martin");

        service.deleteBook(101);

        assertThrows(IllegalArgumentException.class,
                () -> service.getBookById(101));
    }

    @Test
    void testSearchBooks() throws Exception {

        service.addBook(101, "Clean Code", "Robert Martin");
        service.addBook(102, "Database Systems", "Author");

        assertEquals(1, service.searchBooks("clean").size());
    }

    @Test
    void testFilterBooks() throws Exception {

        service.addBook(101, "Clean Code", "Robert Martin");

        service.changeStatus(101, BookStatus.ISSUED);

        assertEquals(1,
                service.filterBooks(BookStatus.ISSUED).size());
    }

    @Test
    void testStatistics() throws Exception {

        service.addBook(101, "Book A", "Author");
        service.addBook(102, "Book B", "Author");
        service.addBook(103, "Book C", "Author");

        service.changeStatus(102, BookStatus.ISSUED);
        service.changeStatus(103, BookStatus.RESERVED);

        LibraryStatistics stats = service.getStatistics();

        assertEquals(3, stats.getTotal());
        assertEquals(1, stats.getAvailable());
        assertEquals(1, stats.getIssued());
        assertEquals(1, stats.getReserved());
    }

    private static class TestBookRepository extends BookRepository {

        private final List<Book> books = new ArrayList<>();

        TestBookRepository() {
            super(null, null);
        }

        @Override
        public List<Book> findAll() {
            return new ArrayList<>(books);
        }

        @Override
        public Book findById(int id) {

            for (Book book : books) {
                if (book.getId() == id) {
                    return book;
                }
            }

            return null;
        }

        @Override
        public Book add(Book book) {
            books.add(book);
            return book;
        }

        @Override
        public Book update(int id, String title, String author) {

            Book book = requireBook(id);

            book.setTitle(title);
            book.setAuthor(author);

            return book;
        }

        @Override
        public Book updateStatus(int id, BookStatus status) {

            Book book = requireBook(id);
            book.setStatus(status);

            return book;
        }

        @Override
        public void delete(int id) {
            books.remove(requireBook(id));
        }

        private Book requireBook(int id) {

            Book book = findById(id);

            if (book == null) {
                throw new IllegalArgumentException("Book not found.");
            }

            return book;
        }
    }
}