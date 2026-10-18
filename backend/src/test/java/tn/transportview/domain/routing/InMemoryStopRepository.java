package tn.transportview.domain.routing;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import tn.transportview.domain.model.Coordinates;
import tn.transportview.domain.model.Stop;
import tn.transportview.domain.port.StopRepositoryPort;

class InMemoryStopRepository implements StopRepositoryPort {

    private final Map<String, Stop> stops;

    InMemoryStopRepository(List<Stop> stops) {
        this.stops = stops.stream().collect(Collectors.toMap(Stop::id, stop -> stop));
    }

    @Override
    public Optional<Stop> findById(String stopId) {
        return Optional.ofNullable(stops.get(stopId));
    }

    @Override
    public List<Stop> findNearby(Coordinates origin, double radiusMeters) {
        return stops.values().stream()
                .filter(stop -> GeoDistance.metersBetween(origin, stop.coordinates()) <= radiusMeters)
                .toList();
    }

    @Override
    public List<Stop> findByNameContaining(String query, int limit) {
        return stops.values().stream()
                .filter(stop -> stop.name().toLowerCase().contains(query.toLowerCase()))
                .limit(limit)
                .toList();
    }
}
