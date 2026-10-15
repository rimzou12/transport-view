package tn.transportview.domain.routing;

import java.time.LocalTime;
import java.util.List;
import tn.transportview.domain.model.Coordinates;
import tn.transportview.domain.model.Itinerary;
import tn.transportview.domain.model.Leg;
import tn.transportview.domain.model.Stop;
import tn.transportview.domain.model.TransportMode;

/**
 * Taxi has no GTFS schedule (see issue #2), so it can't be routed through RAPTOR's transit
 * graph. This estimates a direct taxi leg from straight-line distance and an assumed average
 * urban speed, used as a fallback when no transit itinerary can be found (no nearby stops, or
 * no connecting trips between them).
 */
public final class TaxiLegEstimator {

    private static final double AVERAGE_SPEED_KMH = 20.0;

    private TaxiLegEstimator() {
    }

    public static Itinerary estimateItinerary(Coordinates origin, Coordinates destination, LocalTime departureTime) {
        double distanceMeters = GeoDistance.metersBetween(origin, destination);
        long minutes = Math.max(1, Math.round((distanceMeters / 1000.0) / AVERAGE_SPEED_KMH * 60));
        LocalTime arrivalTime = departureTime.plusMinutes(minutes);

        Stop originStop = new Stop("origin", "Your location", origin);
        Stop destinationStop = new Stop("destination", "Destination", destination);
        Leg taxiLeg = new Leg(TransportMode.TAXI, originStop, destinationStop, departureTime, arrivalTime, null);

        return new Itinerary(List.of(taxiLeg));
    }
}
