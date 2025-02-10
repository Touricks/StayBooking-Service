package com.fmeng48.staybooking.model;


public record LoginRequest(
        String username,
        String password
) {
}
