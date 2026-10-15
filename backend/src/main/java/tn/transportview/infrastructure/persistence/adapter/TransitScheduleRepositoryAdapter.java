package tn.transportview.infrastructure.persistence.adapter;

import java.util.List;
import org.springframework.stereotype.Component;
import tn.transportview.domain.model.Route;
import tn.transportview.domain.model.StopTime;
import tn.transportview.domain.model.Trip;
import tn.transportview.domain.port.TransitScheduleRepositoryPort;
import tn.transportview.infrastructure.persistence.entity.RouteEntity;
import tn.transportview.infrastructure.persistence.entity.StopTimeEntity;
import tn.transportview.infrastructure.persistence.entity.TripEntity;
import tn.transportview.infrastructure.persistence.repository.RouteJpaRepository;
import tn.transportview.infrastructure.persistence.repository.StopTimeJpaRepository;
import tn.transportview.infrastructure.persistence.repository.TripJpaRepository;

@Component
public class TransitScheduleRepositoryAdapter implements TransitScheduleRepositoryPort {

    private final StopTimeJpaRepository stopTimeJpaRepository;
    private final TripJpaRepository tripJpaRepository;
    private final RouteJpaRepository routeJpaRepository;

    public TransitScheduleRepositoryAdapter(
            StopTimeJpaRepository stopTimeJpaRepository,
            TripJpaRepository tripJpaRepository,
            RouteJpaRepository routeJpaRepository) {
        this.stopTimeJpaRepository = stopTimeJpaRepository;
        this.tripJpaRepository = tripJpaRepository;
        this.routeJpaRepository = routeJpaRepository;
    }

    @Override
    public List<StopTime> findStopTimesForStop(String stopId) {
        return stopTimeJpaRepository.findByStopIdOrderByStopSequence(stopId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<StopTime> findStopTimesForTrip(String tripId) {
        return stopTimeJpaRepository.findByTripIdOrderByStopSequence(tripId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Trip findTrip(String tripId) {
        TripEntity entity = tripJpaRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + tripId));
        return new Trip(entity.getId(), entity.getRouteId(), entity.getHeadsign());
    }

    @Override
    public Route findRouteForTrip(String tripId) {
        TripEntity trip = tripJpaRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + tripId));
        RouteEntity route = routeJpaRepository.findById(trip.getRouteId())
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + trip.getRouteId()));
        return new Route(route.getId(), route.getShortName(), route.getLongName(), route.getMode());
    }

    private StopTime toDomain(StopTimeEntity entity) {
        return new StopTime(entity.getTripId(), entity.getStopId(), entity.getStopSequence(), entity.getArrival(), entity.getDeparture());
    }
}
