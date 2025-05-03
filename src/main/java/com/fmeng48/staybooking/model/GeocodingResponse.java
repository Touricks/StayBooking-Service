package com.fmeng48.staybooking.model;

public record GeocodingResponse(
    double lat,
    double lon,
    String formattedAddress
) {} 