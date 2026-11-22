package com.library.model;

/**
 * Represents the librarian profile associated with a Supabase Auth user.
 *
 * @author Ibrahim Muhammad Abbas
 */
public class Librarian {

    private String id;
    private String name;

    /**
     * Creates a librarian profile with its Auth user UUID and display name.
     * @param id librarian profile UUID
     * @param name setting name, column heading or librarian name
     */
    public Librarian(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Returns the id.
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name.
     * @return the name
     */
    public String getName() {
        return name;
    }
}