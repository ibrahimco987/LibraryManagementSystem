package com.library.repository;

import com.library.config.SupabaseClient;
import com.library.model.Book;
import com.library.model.BookStatus;
import com.library.service.AuthenticationService;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BookRepository {

    private final SupabaseClient client;
    private final AuthenticationService auth;
    private final Gson gson;

    private static final String TABLE = "/rest/v1/books";

    public BookRepository(SupabaseClient client,
                          AuthenticationService auth) {

        this.client = client;
        this.auth = auth;
        this.gson = new Gson();
    }

    public List<Book> findAll()
            throws IOException, InterruptedException {

        String response = client.request(
                "GET",
                TABLE + "?order=id.asc",
                null,
                auth.getAccessToken()
        );

        Book[] books = gson.fromJson(response, Book[].class);

        return Arrays.asList(books);
    }

    public Book findById(int id)
            throws IOException, InterruptedException {

        String response = client.request(
                "GET",
                TABLE + "?id=eq." + id,
                null,
                auth.getAccessToken()
        );

        Book[] books = gson.fromJson(response, Book[].class);

        if (books.length == 0) {
            return null;
        }

        return books[0];
    }

    public Book add(Book book)
            throws IOException, InterruptedException {

        String response = client.request(
                "POST",
                TABLE,
                gson.toJson(book),
                auth.getAccessToken()
        );

        return getAffectedBook(response);
    }

    public Book update(int id, String title, String author)
            throws IOException, InterruptedException {

        JsonObject body = new JsonObject();

        body.addProperty("title", title);
        body.addProperty("author", author);

        String response = client.request(
                "PATCH",
                TABLE + "?id=eq." + id,
                body.toString(),
                auth.getAccessToken()
        );

        return getAffectedBook(response);
    }

    public Book updateStatus(int id, BookStatus status)
            throws IOException, InterruptedException {

        JsonObject body = new JsonObject();

        body.add("status", gson.toJsonTree(status));

        String response = client.request(
                "PATCH",
                TABLE + "?id=eq." + id,
                body.toString(),
                auth.getAccessToken()
        );

        return getAffectedBook(response);
    }

    public void delete(int id)
            throws IOException, InterruptedException {

        String response = client.request(
                "DELETE",
                TABLE + "?id=eq." + id,
                null,
                auth.getAccessToken()
        );

        getAffectedBook(response);
    }

    private Book getAffectedBook(String response) {

        Book[] books = gson.fromJson(response, Book[].class);

        if (books == null || books.length == 0) {
            throw new IllegalArgumentException(
                    "Book not found or access denied."
            );
        }

        return books[0];
    }
}