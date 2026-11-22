package com.library.model;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a database-generated book activity event returned by Supabase.
 *
 * @author Ibrahim Muhammad Abbas
 */
public class ActivityLog {
    /** Creates an empty event whose fields can be populated by JSON deserialization. */
    public ActivityLog() {}


    private long id;

    @SerializedName("librarian_id")
    private String librarianId;

    @SerializedName("librarian_name")
    private String librarianName;

    @SerializedName("book_id")
    private Integer bookId;

    private String action;

    @SerializedName("created_at")
    private String createdAt;

    /**
     * Returns the id.
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the librarian id.
     * @return the librarian id
     */
    public String getLibrarianId() {
        return librarianId;
    }

    /**
     * Returns the librarian name.
     * @return the librarian name, or null when absent
     */
    public String getLibrarianName() {
        return librarianName;
    }

    /**
     * Returns the book id.
     * @return the book id, or null when absent
     */
    public Integer getBookId() {
        return bookId;
    }

    /**
     * Returns the action.
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * Returns the creation timestamp supplied by Supabase.
     * @return the timestamp string, or null before persistence
     */
    public String getCreatedAt() {
        return createdAt;
    }
}