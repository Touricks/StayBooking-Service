package com.fmeng48.staybooking.model;


public record RegisterRequest(
        String username,
        String password,
        UserRole role
) {
}
