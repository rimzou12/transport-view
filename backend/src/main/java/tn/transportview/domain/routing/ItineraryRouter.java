package tn.transportview.domain.routing;

import java.util.List;
import tn.transportview.domain.model.Itinerary;

/**
 * Computes multi-leg public transit itineraries between an origin and a destination.
 * Implementations must stay free of framework/infrastructure dependencies.
 */
public interface ItineraryRouter {

    List<Itinerary> search(JourneyRequest request);
}
