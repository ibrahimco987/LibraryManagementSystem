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

/**
 * Maps book operations to authenticated Supabase REST requests without duplicating service validation.
 *
 * @author Ibrahim Muhammad Abbas
 */
public class BookRepository {

    private final SupabaseClient client;
    private final AuthenticationService auth;
    private final Gson gson;

    private static final String TABLE = "/rest/v1/books";

    /**
     * Connects book persistence to the configured client and librarian session.
     * @param client configured Supabase HTTP client
     * @param auth authentication service for the active librarian
     */
    public BookRepository(SupabaseClient client,
                          AuthenticationService auth) {

        this.client = client;
        this.auth = auth;
        this.gson = new Gson();
    }

    /**
     * Fetches all book rows ordered by ascending ID.
     * @return the fetched books
     * @throws IOException if the HTTP request fails or Supabase rejects it
     * @throws InterruptedException if the request is interrupted
     */
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

    /**
     * Fetches a book by its ID.
     * @param id positive book ID
     * @return the matching book, or null when no row is returned
     * @throws IOException if the HTTP request fails or Supabase rejects it
     * @throws InterruptedException if the request is interrupted
     */
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

    /**
     * Posts a book and requires an affected row in the response.
     * @param book book to persist or display
     * @return the persisted book
     * @throws IOException if the HTTP request fails or Supabase rejects it
     * @throws InterruptedException if the request is interrupted
     */
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

    /**
     * Patches the title and author and requires an affected row.
     * @param id positive book ID
     * @param title book title
     * @param author book author
     * @return the updated book
     * @throws IOException if the HTTP request fails or Supabase rejects it
     * @throws InterruptedException if the request is interrupted
     */
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

    /**
     * Patches only the status and requires an affected row.
     * @param id positive book ID
     * @param status book status
     * @return the updated book
     * @throws IOException if the HTTP request fails or Supabase rejects it
     * @throws InterruptedException if the request is interrupted
     */
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

    /**
     * Deletes the selected book and rejects a response with no affected row.
     * @param id positive book ID
     * @throws IOException if the HTTP request fails or Supabase rejects it
     * @throws InterruptedException if the request is interrupted
     */
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

    /**
     * Parses a mutation response and rejects an empty result.
     * @param response JSON response body
     * @return the first affected book
     */
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