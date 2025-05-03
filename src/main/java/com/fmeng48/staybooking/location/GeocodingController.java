package com.fmeng48.staybooking.location;

import com.fmeng48.staybooking.model.GeocodingResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/geocoding")
public class GeocodingController {
    private final GeocodingService geocodingService;

    public GeocodingController(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    @GetMapping("/address")
    public ResponseEntity<GeocodingResponse> getGeocodingInfo(@RequestParam String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new GeocodingException("Address cannot be empty");
        }
        GeocodingResponse response = geocodingService.getGeocodingInfo(address);
        return ResponseEntity.ok(response);
    }
} 