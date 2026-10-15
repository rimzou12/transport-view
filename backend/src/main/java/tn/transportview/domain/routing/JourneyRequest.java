package tn.transportview.domain.routing;

import java.time.LocalTime;
import tn.transportview.domain.model.Coordinates;

public record JourneyRequest(Coordinates origin, Coordinates destination, LocalTime departureTime) {
}
