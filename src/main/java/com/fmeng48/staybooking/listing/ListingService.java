package com.fmeng48.staybooking.listing;


import com.fmeng48.staybooking.booking.BookingService;
import com.fmeng48.staybooking.location.GeocodingService;
import com.fmeng48.staybooking.model.GeoPoint;
import com.fmeng48.staybooking.model.ListingDto;
import com.fmeng48.staybooking.model.ListingEntity;
import com.fmeng48.staybooking.storage.ImageStorageService;
import com.fmeng48.staybooking.repository.ListingRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDate;
import java.util.List;


@Service
public class ListingService {
    private final BookingService bookingService;
    private final GeocodingService geocodingService;
    private final ImageStorageService imageStorageService;
    private final ListingRepository listingRepository;


    public ListingService(
            BookingService bookingService,
            GeocodingService geocodingService,
            ImageStorageService imageStorageService,
            ListingRepository listingRepository
    ) {
        this.bookingService = bookingService;
        this.geocodingService = geocodingService;
        this.imageStorageService = imageStorageService;
        this.listingRepository = listingRepository;
    }


    public List<ListingDto> getListings(Long hostId) {
        return listingRepository.findAllByHostId(hostId)
                .stream()
                .map(ListingDto::new)
                .toList();
    }


    public void createListing(
            long hostId,
            String name,
            String address,
            String description,
            int guestNumber,
            List<MultipartFile> images)
    {
        // Input validation
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidListingException("Listing name cannot be empty");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new InvalidListingException("Address cannot be empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new InvalidListingException("Description cannot be empty");
        }
        if (guestNumber <= 0) {
            throw new InvalidListingException("Guest number must be positive");
        }
        if (images == null || images.isEmpty()) {
            throw new InvalidListingException("At least one image is required");
        }

        // Image upload with exception handling
        List<String> uploadedUrls;
        try {
            uploadedUrls = images.parallelStream()
                    .filter(image -> !image.isEmpty())
                    .map(imageStorageService::upload)
                    .toList();
        } catch (Exception e) {
            throw new InvalidListingException("Failed to upload images: " + e.getMessage());
        }

        // Geocoding with exception handling
        GeoPoint geoPoint;
        try {
            geoPoint = geocodingService.getGeoPoint(address);
        } catch (Exception e) {
            throw new InvalidListingException("Failed to geocode address: " + e.getMessage());
        }

        GeometryFactory geometryFactory = new GeometryFactory();

        ListingEntity newHouse = new ListingEntity(
                null,
                hostId,
                name,
                address,
                description,
                guestNumber,
                uploadedUrls,
                geometryFactory.createPoint(new Coordinate(geoPoint.lon(), geoPoint.lat()))
        );

        // Database operation with exception handling
        try {
            listingRepository.save(newHouse);
        } catch (Exception e) {
            throw new InvalidListingException("Failed to save listing: " + e.getMessage());
        }
    }

    public void deleteListing(long hostId, long listingId) {
        ListingEntity listing = listingRepository.getReferenceById(listingId);
        if (listing.getHostId() != hostId) {
            throw new DeleteListingNotAllowedException("Host " + hostId + " not allowed to delete listing " + listingId);
        }
        if (bookingService.existsActiveBookings(listingId)) {
            throw new DeleteListingNotAllowedException("Active bookings exist, not allowed to delete listing " + listingId);
        }
        listingRepository.deleteById(listingId);
    }


    public List<ListingDto> search(
            double lat,
            double lon,
            int distance,
            LocalDate checkIn,
            LocalDate checkOut,
            int guestNum
    ) {
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            throw new InvalidListingSearchException("Invalid latitude or longitude.");
        }
        if (distance <= 0) {
            throw new InvalidListingSearchException("Distance must be positive.");
        }
        if (checkIn.isAfter(checkOut)) {
            throw new InvalidListingSearchException("Check-in date must be before check-out date.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new InvalidListingSearchException("Check-in date must be in the future.");
        }
        return listingRepository.searchListings(
                        lat,
                        lon,
                        distance,
                        checkIn,
                        checkOut,
                        guestNum
                )
                .stream()
                .map(ListingDto::new)
                .toList();


    }
}
