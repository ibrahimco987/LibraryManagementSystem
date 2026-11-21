package com.library.model;

import com.google.gson.annotations.SerializedName;

public class ActivityLog {

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

    public long getId() {
        return id;
    }

    public String getLibrarianId() {
        return librarianId;
    }

    public String getLibrarianName() {
        return librarianName;
    }

    public Integer getBookId() {
        return bookId;
    }

    public String getAction() {
        return action;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}