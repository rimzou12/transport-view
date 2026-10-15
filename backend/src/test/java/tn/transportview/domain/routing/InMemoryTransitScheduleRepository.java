package tn.transportview.domain.routing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import tn.transportview.domain.model.Route;
import tn.transportview.domain.model.StopTime;
import tn.transportview.domain.model.Trip;
import tn.transportview.domain.port.TransitScheduleRepositoryPort;

class InMemoryTransitScheduleRepository implements TransitScheduleRepositoryPort {

    private final Map<String, List<StopTime>> stopTimesByStop = new HashMap<>();
    private final Map<String, List<StopTime>> stopTimesByTrip = new HashMap<>();
    private final Map<String, Trip> tripsById;
    private final Map<String, Route> routesById;

    InMemoryTransitScheduleRepository(List<StopTime> stopTimes, List<Trip> trips, List<Route> routes) {
        for (StopTime stopTime : stopTimes) {
            stopTimesByStop.computeIfAbsent(stopTime.stopId(), key -> new ArrayList<>()).add(stopTime);
            stopTimesByTrip.computeIfAbsent(stopTime.tripId(), key -> new ArrayList<>()).add(stopTime);
        }
        stopTimesByTrip.values().forEach(list -> list.sort(Comparator.comparingInt(StopTime::stopSequence)));
        this.tripsById = trips.stream().collect(Collectors.toMap(Trip::id, trip -> trip));
        this.routesById = routes.stream().collect(Collectors.toMap(Route::id, route -> route));
    }

    @Override
    public List<StopTime> findStopTimesForStop(String stopId) {
        return stopTimesByStop.getOrDefault(stopId, List.of());
    }

    @Override
    public List<StopTime> findStopTimesForTrip(String tripId) {
        return stopTimesByTrip.getOrDefault(tripId, List.of());
    }

    @Override
    public Trip findTrip(String tripId) {
        return tripsById.get(tripId);
    }

    @Override
    public Route findRouteForTrip(String tripId) {
        return routesById.get(tripsById.get(tripId).routeId());
    }
}
