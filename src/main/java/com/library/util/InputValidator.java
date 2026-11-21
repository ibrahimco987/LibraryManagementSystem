package com.library.util;

import com.library.model.BookStatus;

public class InputValidator {

    public static void validateId(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Book ID must be positive."
            );
        }
    }

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

    public static void validateStatus(BookStatus status) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Book status cannot be empty."
            );
        }
    }
}