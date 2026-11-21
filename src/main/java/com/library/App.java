package com.library;

import com.library.config.SupabaseClient;
import com.library.service.AuthenticationService;
import com.library.service.BookService;
import com.library.repository.BookRepository;
import com.library.model.Book;
import com.library.model.BookStatus;

import java.io.Console;

public class App {

    public static void main(String[] args) {

        try {
            SupabaseClient client = new SupabaseClient();

            AuthenticationService auth =
                    new AuthenticationService(client);

            Console console = System.console();

            if (console == null) {
                System.out.println("Run from a terminal.");
                return;
            }

            String email = console.readLine("Enter email: ");

            String password = new String(
                    console.readPassword("Enter password: ")
            );

            auth.login(email, password);

            try {

                BookRepository repository =
                        new BookRepository(client, auth);

                BookService service =
                        new BookService(repository);

                int id = Integer.parseInt(
                        console.readLine("Enter an unused test book ID: ")
                );

                service.addBook(id, "Clean Code", "Robert Martin");

                System.out.println("Book added successfully.");

                for (Book book : service.getAllBooks()) {

                    System.out.println(
                            book.getId() + " | "
                            + book.getTitle() + " | "
                            + book.getAuthor() + " | "
                            + book.getStatus()
                    );
                }

                service.updateBook(
                        id, "Clean Code - Updated", "Robert Martin"
                );

                System.out.println("Book updated successfully.");

                service.changeStatus(id, BookStatus.ISSUED);

                System.out.println(
                        "New status: " +
                        service.getBookById(id).getStatus()
                );

                service.deleteBook(id);

                System.out.println("Test book deleted successfully.");

            } finally {
                auth.logout();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}