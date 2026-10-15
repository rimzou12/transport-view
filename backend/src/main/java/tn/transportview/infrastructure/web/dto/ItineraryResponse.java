package tn.transportview.infrastructure.web.dto;

import java.util.List;

public record ItineraryResponse(List<LegResponse> legs, long totalDurationSeconds, int transferCount) {

    public record LegResponse(
            String mode,
            String fromStopName,
            double fromStopLatitude,
            double fromStopLongitude,
            String toStopName,
            double toStopLatitude,
            double toStopLongitude,
            String departure,
            String arrival,
            String routeShortName) {
    }
}
