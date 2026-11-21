package com.library.model;

public class LibraryStatistics {

    private final int total;
    private final int available;
    private final int issued;
    private final int reserved;

    public LibraryStatistics(int total, int available,
                             int issued, int reserved) {

        this.total = total;
        this.available = available;
        this.issued = issued;
        this.reserved = reserved;
    }

    public int getTotal() {
        return total;
    }

    public int getAvailable() {
        return available;
    }

    public int getIssued() {
        return issued;
    }

    public int getReserved() {
        return reserved;
    }
}