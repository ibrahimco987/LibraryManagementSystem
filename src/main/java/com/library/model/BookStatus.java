package com.library.model;

import com.google.gson.annotations.SerializedName;

/**
 * Defines the three book statuses and their serialized database values.
 *
 * @author Ibrahim Muhammad Abbas
 */
public enum BookStatus {

    /** The book is available. */
    @SerializedName("Available")
    AVAILABLE,

    /** The book is currently issued. */
    @SerializedName("Issued")
    ISSUED,

    /** The book is reserved. */
    @SerializedName("Reserved")
    RESERVED
}