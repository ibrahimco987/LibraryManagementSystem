package com.library.util;

import com.library.model.BookStatus;

/**
 * Checks book identifiers, required text fields and status values before service operations.
 *
 * @author Ibrahim Muhammad Abbas
 */
public class InputValidator {
    /** Creates a validator object; validation entry points are static. */
    public InputValidator() {}


    /**
     * Rejects identifiers that are zero or negative.
     * @param id positive book ID
     * @throws IllegalArgumentException if the supplied value is invalid
     */
    public static void validateId(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Book ID must be positive."
            );
        }
    }

    /**
     * Rejects null, empty or whitespace-only titles and authors.
     * @param title book title
     * @param author book author
     * @throws IllegalArgumentException if the supplied value is invalid
     */
    public static void validateDetails(String title, String author) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Book title cannot be empty."
            );
        }

        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException(
                    "Author name cannot be empty."
            );
        }
    }

    /**
     * Rejects a null status.
     * @param status book status
     * @throws IllegalArgumentException if the supplied value is invalid
     */
    public static void validateStatus(BookStatus status) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Book status cannot be empty."
            );
        }
    }
}