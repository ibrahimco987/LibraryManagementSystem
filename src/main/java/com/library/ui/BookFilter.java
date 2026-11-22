package com.library.ui;
import com.library.model.*;
import java.util.*;
public final class BookFilter {
    private BookFilter() {}
    public static List<Book> apply(List<Book> books, String query, BookStatus status) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return books.stream().filter(b -> status == null || b.getStatus() == status)
            .filter(b -> String.valueOf(b.getId()).contains(q) || b.getTitle().toLowerCase(Locale.ROOT).contains(q)
                || b.getAuthor().toLowerCase(Locale.ROOT).contains(q)).toList();
    }
}
