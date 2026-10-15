package tn.transportview.domain.routing;

import java.util.List;
import tn.transportview.domain.model.Itinerary;
import tn.transportview.domain.port.StopRepositoryPort;
import tn.transportview.domain.port.TransitScheduleRepositoryPort;

/**
 * RAPTOR-based implementation of {@link ItineraryRouter}.
 * Algorithm implementation tracked separately (see ticket: RAPTOR routing engine).
 */
public class RaptorItineraryRouter implements ItineraryRouter {

    private final StopRepositoryPort stopRepositoryPort;
    private final TransitScheduleRepositoryPort scheduleRepositoryPort;

    public RaptorItineraryRouter(StopRepositoryPort stopRepositoryPort, TransitScheduleRepositoryPort scheduleRepositoryPort) {
        this.stopRepositoryPort = stopRepositoryPort;
        this.scheduleRepositoryPort = scheduleRepositoryPort;
    }

    @Override
    public List<Itinerary> search(JourneyRequest request) {
        throw new UnsupportedOperationException("RAPTOR routing not yet implemented");
    }
}
