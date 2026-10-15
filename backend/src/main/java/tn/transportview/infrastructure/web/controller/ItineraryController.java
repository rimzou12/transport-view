package tn.transportview.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.transportview.application.usecase.SearchItinerariesUseCase;
import tn.transportview.domain.model.Coordinates;
import tn.transportview.domain.model.Itinerary;
import tn.transportview.domain.routing.GeoDistance;
import tn.transportview.domain.routing.JourneyRequest;
import tn.transportview.infrastructure.web.dto.ApiError;
import tn.transportview.infrastructure.web.dto.ItineraryResponse;
import tn.transportview.infrastructure.web.dto.ItinerarySearchRequest;

@Tag(name = "Itineraries", description = "Search multi-modal journeys (metro, TGM, taxi fallback) across Tunis")
@RestController
@RequestMapping("/api/v1/itineraries")
public class ItineraryController {

    /**
     * Sanity bound on origin/destination distance — this plans journeys within greater Tunis,
     * not nationwide trips. 100km generously covers the metro area and its outskirts.
     */
    private static final double MAX_JOURNEY_DISTANCE_METERS = 100_000;

    private final SearchItinerariesUseCase searchItinerariesUseCase;

    public ItineraryController(SearchItinerariesUseCase searchItinerariesUseCase) {
        this.searchItinerariesUseCase = searchItinerariesUseCase;
    }

    @Operation(summary = "Search itineraries between an origin and a destination")
    @ApiResponse(responseCode = "200", description = "Ranked itineraries (Pareto frontier over arrival time / transfer count)")
    @ApiResponse(responseCode = "400", description = "Invalid request (bad coordinates, malformed time, origin/destination too far apart)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping
    public List<ItineraryResponse> search(@Valid @RequestBody ItinerarySearchRequest request) {
        Coordinates origin = new Coordinates(request.originLatitude(), request.originLongitude());
        Coordinates destination = new Coordinates(request.destinationLatitude(), request.destinationLongitude());
        requireWithinServiceArea(origin, destination);

        JourneyRequest journeyRequest = new JourneyRequest(origin, destination, parseDepartureTime(request.departureTime()));

        return searchItinerariesUseCase.execute(journeyRequest).stream()
                .map(this::toResponse)
                .toList();
    }

    private void requireWithinServiceArea(Coordinates origin, Coordinates destination) {
        if (GeoDistance.metersBetween(origin, destination) > MAX_JOURNEY_DISTANCE_METERS) {
            throw new IllegalArgumentException("Origin and destination are too far apart for this service area");
        }
    }

    private LocalTime parseDepartureTime(String rawDepartureTime) {
        if (rawDepartureTime == null) {
            return LocalTime.now();
        }
        try {
            return LocalTime.parse(rawDepartureTime);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid departureTime format, expected HH:mm:ss");
        }
    }

    private ItineraryResponse toResponse(Itinerary itinerary) {
        List<ItineraryResponse.LegResponse> legs = itinerary.legs().stream()
                .map(leg -> new ItineraryResponse.LegResponse(
                        leg.mode().name(),
                        leg.fromStop().name(),
                        leg.fromStop().coordinates().latitude(),
                        leg.fromStop().coordinates().longitude(),
                        leg.toStop().name(),
                        leg.toStop().coordinates().latitude(),
                        leg.toStop().coordinates().longitude(),
                        leg.departure().toString(),
                        leg.arrival().toString(),
                        leg.routeShortName()))
                .toList();

        return new ItineraryResponse(legs, itinerary.totalDuration().toSeconds(), itinerary.transferCount());
    }
}
