package com.library.model;

import com.google.gson.annotations.SerializedName;

public enum BookStatus {

    @SerializedName("Available")
    AVAILABLE,

    @SerializedName("Issued")
    ISSUED,

    @SerializedName("Reserved")
    RESERVED
}