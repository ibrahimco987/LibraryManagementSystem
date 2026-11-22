package com.library.model;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a library book. New instances start with Available status; services validate values before persistence.
 *
 * @author Ibrahim Muhammad Abbas
 */
public class Book {

    private int id;
    private String title;
    private String author;
    private BookStatus status;

    @SerializedName("created_at")
    private String createdAt;

    /**
     * Creates a book with Available status.
     * @param id positive book ID
     * @param title book title
     * @param author book author
     */
    public Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.status = BookStatus.AVAILABLE;
    }

    /**
     * Returns the id.
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the title.
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the author.
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Returns the status.
     * @return the status
     */
    public BookStatus getStatus() {
        return status;
    }

    /**
     * Returns the creation timestamp supplied by Supabase.
     * @return the timestamp string, or null before persistence
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Assigns the title; service validation is performed before persistence.
     * @param title book title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Assigns the author; service validation is performed before persistence.
     * @param author book author
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Assigns the status; service validation is performed before persistence.
     * @param status book status
     */
    public void setStatus(BookStatus status) {
        this.status = status;
    }
}