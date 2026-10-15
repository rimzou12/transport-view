package tn.transportview.infrastructure.web.controller;

import jakarta.validation.Valid;
import java.time.LocalTime;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.transportview.application.usecase.SearchItinerariesUseCase;
import tn.transportview.domain.model.Coordinates;
import tn.transportview.domain.model.Itinerary;
import tn.transportview.domain.routing.JourneyRequest;
import tn.transportview.infrastructure.web.dto.ItineraryResponse;
import tn.transportview.infrastructure.web.dto.ItinerarySearchRequest;

@RestController
@RequestMapping("/api/v1/itineraries")
public class ItineraryController {

    private final SearchItinerariesUseCase searchItinerariesUseCase;

    public ItineraryController(SearchItinerariesUseCase searchItinerariesUseCase) {
        this.searchItinerariesUseCase = searchItinerariesUseCase;
    }

    @PostMapping
    public List<ItineraryResponse> search(@Valid @RequestBody ItinerarySearchRequest request) {
        JourneyRequest journeyRequest = new JourneyRequest(
                new Coordinates(request.originLatitude(), request.originLongitude()),
                new Coordinates(request.destinationLatitude(), request.destinationLongitude()),
                request.departureTime() != null ? LocalTime.parse(request.departureTime()) : LocalTime.now());

        return searchItinerariesUseCase.execute(journeyRequest).stream()
                .map(this::toResponse)
                .toList();
    }

    private ItineraryResponse toResponse(Itinerary itinerary) {
        List<ItineraryResponse.LegResponse> legs = itinerary.legs().stream()
                .map(leg -> new ItineraryResponse.LegResponse(
                        leg.mode().name(),
                        leg.fromStop().name(),
                        leg.toStop().name(),
                        leg.departure().toString(),
                        leg.arrival().toString(),
                        leg.routeShortName()))
                .toList();

        return new ItineraryResponse(legs, itinerary.totalDuration().toSeconds(), itinerary.transferCount());
    }
}
