package com.library.util;

import org.junit.jupiter.api.Test;
import com.library.model.BookStatus;

import static org.junit.jupiter.api.Assertions.*;

class InputValidatorTest {

    @Test
    void testValidInput() {

        assertDoesNotThrow(() -> {
            InputValidator.validateId(101);
            InputValidator.validateDetails("Clean Code", "Robert Martin");
            InputValidator.validateStatus(BookStatus.AVAILABLE);
        });
    }

    @Test
    void testInvalidId() {

        assertThrows(IllegalArgumentException.class,
                () -> InputValidator.validateId(-1));
    }

    @Test
    void testEmptyTitle() {

        assertThrows(IllegalArgumentException.class,
                () -> InputValidator.validateDetails("", "Robert Martin"));
    }

    @Test
    void testEmptyAuthor() {

        assertThrows(IllegalArgumentException.class,
                () -> InputValidator.validateDetails("Clean Code", ""));
    }

    @Test
    void testInvalidStatus() {

        assertThrows(IllegalArgumentException.class,
                () -> InputValidator.validateStatus(null));
    }
}