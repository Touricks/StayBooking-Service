package com.fmeng48.staybooking.location;

import com.fmeng48.staybooking.model.GeoPoint;
import com.fmeng48.staybooking.model.GeocodingResponse;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class GeocodingService {
    private final GeoApiContext context;

    public GeocodingService(@Value("${staybooking.geocoding.key}") String apiKey) {
        this.context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    public GeoPoint getGeoPoint(String address) {
        try {
            GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
            if (results == null || results.length == 0) {
                throw new GeocodingException("No results found for address: " + address);
            }
            if (results[0].partialMatch) {
                throw new GeocodingException("Address is not precise enough: " + address);
            }
            return new GeoPoint(
                results[0].geometry.location.lat,
                results[0].geometry.location.lng
            );
        } catch (Exception e) {
            throw new GeocodingException("Failed to geocode address: " + address, e);
        }
    }

    public GeocodingResponse getGeocodingInfo(String address) {
        try {
            GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
            if (results == null || results.length == 0) {
                throw new GeocodingException("No results found for address: " + address);
            }
            if (results[0].partialMatch) {
                throw new GeocodingException("Address is not precise enough: " + address);
            }
            return new GeocodingResponse(
                results[0].geometry.location.lat,
                results[0].geometry.location.lng,
                results[0].formattedAddress
            );
        } catch (Exception e) {
            throw new GeocodingException("Failed to geocode address: " + address, e);
        }
    }
}
