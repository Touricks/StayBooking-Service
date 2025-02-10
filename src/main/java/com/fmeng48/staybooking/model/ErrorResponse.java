package com.fmeng48.staybooking.model;


public record ErrorResponse(
        String message,
        String error
) {
}
