package tn.transportview.domain.port;

import java.util.List;
import tn.transportview.domain.model.Route;
import tn.transportview.domain.model.StopTime;
import tn.transportview.domain.model.Trip;

public interface TransitScheduleRepositoryPort {

    List<StopTime> findStopTimesForStop(String stopId);

    List<StopTime> findStopTimesForTrip(String tripId);

    Trip findTrip(String tripId);

    Route findRouteForTrip(String tripId);
}
