package com.library.model;

/**
 * Stores an immutable snapshot of total and per-status book counts.
 *
 * @author Ibrahim Muhammad Abbas
 */
public class LibraryStatistics {

    private final int total;
    private final int available;
    private final int issued;
    private final int reserved;

    /**
     * Creates an immutable snapshot of the total and per-status book counts.
     * @param total total number of books
     * @param available available-book count
     * @param issued issued-book count
     * @param reserved reserved-book count
     */
    public LibraryStatistics(int total, int available,
                             int issued, int reserved) {

        this.total = total;
        this.available = available;
        this.issued = issued;
        this.reserved = reserved;
    }

    /**
     * Returns the total.
     * @return the total
     */
    public int getTotal() {
        return total;
    }

    /**
     * Returns the available.
     * @return the available
     */
    public int getAvailable() {
        return available;
    }

    /**
     * Returns the issued.
     * @return the issued
     */
    public int getIssued() {
        return issued;
    }

    /**
     * Returns the reserved.
     * @return the reserved
     */
    public int getReserved() {
        return reserved;
    }
}