package com.library.ui;
import com.library.model.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class BookFilterTest {
    @Test void combinesTextAndStatusWithoutChangingSource() {
        Book a = new Book(101,"Effective Java","Joshua Bloch");
        Book b = new Book(102,"Java Basics","Other"); b.setStatus(BookStatus.ISSUED);
        List<Book> source = List.of(a,b);
        assertEquals(List.of(b),BookFilter.apply(source," JAVA ",BookStatus.ISSUED));
        assertEquals(List.of(a),BookFilter.apply(source,"BLOCH",null));
        assertEquals(List.of(a,b),BookFilter.apply(source,"10",null));
        assertTrue(BookFilter.apply(source,"unknown",null).isEmpty());
        assertEquals(2,source.size());
    }
    @Test void emptyQueryAndAllStatusReturnAll() {
        Book a = new Book(1,"Title","Author");
        assertEquals(List.of(a),BookFilter.apply(List.of(a),null,null));
        assertTrue(BookFilter.apply(List.of(),"",BookStatus.RESERVED).isEmpty());
    }
}
