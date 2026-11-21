package com.library.model;

import com.google.gson.annotations.SerializedName;

public class Book {

    private int id;
    private String title;
    private String author;
    private BookStatus status;

    @SerializedName("created_at")
    private String createdAt;

    public Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.status = BookStatus.AVAILABLE;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public BookStatus getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }
}