package tn.transportview.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;

public record ItinerarySearchRequest(
        @NotNull Double originLatitude,
        @NotNull Double originLongitude,
        @NotNull Double destinationLatitude,
        @NotNull Double destinationLongitude,
        String departureTime) {
}
