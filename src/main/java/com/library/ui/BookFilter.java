package com.library.ui;
import com.library.model.*;
import java.util.*;
/**
 * Combines case-insensitive text matching and optional status filtering over a fetched collection.
 *
 * @author Maaz
 */
public final class BookFilter {
    /**
     * Prevents instantiation of this static filtering utility.
     */
    private BookFilter() {}
    /**
     * Selects books matching both the optional status and an ID, title or author substring without modifying the source list.
     * @param books source collection
     * @param query search text; null or blank matches all text
     * @param status book status; null means all statuses only for BookFilter
     * @return an unmodifiable list of matching books
     */
    public static List<Book> apply(List<Book> books, String query, BookStatus status) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return books.stream().filter(b -> status == null || b.getStatus() == status)
            .filter(b -> String.valueOf(b.getId()).contains(q) || b.getTitle().toLowerCase(Locale.ROOT).contains(q)
                || b.getAuthor().toLowerCase(Locale.ROOT).contains(q)).toList();
    }
}
