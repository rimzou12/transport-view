package tn.transportview.infrastructure.web.dto;

import java.util.List;

public record ItineraryResponse(List<LegResponse> legs, long totalDurationSeconds, int transferCount) {

    public record LegResponse(
            String mode,
            String fromStopName,
            String toStopName,
            String departure,
            String arrival,
            String routeShortName) {
    }
}
