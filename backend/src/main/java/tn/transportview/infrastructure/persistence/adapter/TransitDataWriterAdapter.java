package tn.transportview.infrastructure.persistence.adapter;

import java.util.List;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.transportview.domain.model.Route;
import tn.transportview.domain.model.Stop;
import tn.transportview.domain.model.StopTime;
import tn.transportview.domain.model.Trip;
import tn.transportview.domain.port.TransitDataWriterPort;
import tn.transportview.infrastructure.persistence.entity.RouteEntity;
import tn.transportview.infrastructure.persistence.entity.StopEntity;
import tn.transportview.infrastructure.persistence.entity.StopTimeEntity;
import tn.transportview.infrastructure.persistence.entity.TripEntity;
import tn.transportview.infrastructure.persistence.repository.RouteJpaRepository;
import tn.transportview.infrastructure.persistence.repository.StopJpaRepository;
import tn.transportview.infrastructure.persistence.repository.StopTimeJpaRepository;
import tn.transportview.infrastructure.persistence.repository.TripJpaRepository;

/**
 * Upserts GTFS-derived domain data. Entities carry assigned (not generated) String ids, so
 * {@code JpaRepository.save()} performs an upsert — safe to re-run an import without duplicating rows.
 */
@Component
public class TransitDataWriterAdapter implements TransitDataWriterPort {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private final StopJpaRepository stopJpaRepository;
    private final RouteJpaRepository routeJpaRepository;
    private final TripJpaRepository tripJpaRepository;
    private final StopTimeJpaRepository stopTimeJpaRepository;

    public TransitDataWriterAdapter(
            StopJpaRepository stopJpaRepository,
            RouteJpaRepository routeJpaRepository,
            TripJpaRepository tripJpaRepository,
            StopTimeJpaRepository stopTimeJpaRepository) {
        this.stopJpaRepository = stopJpaRepository;
        this.routeJpaRepository = routeJpaRepository;
        this.tripJpaRepository = tripJpaRepository;
        this.stopTimeJpaRepository = stopTimeJpaRepository;
    }

    @Override
    @Transactional
    public void saveStops(List<Stop> stops) {
        stopJpaRepository.saveAll(stops.stream().map(this::toEntity).toList());
    }

    @Override
    @Transactional
    public void saveRoutes(List<Route> routes) {
        routeJpaRepository.saveAll(routes.stream().map(this::toEntity).toList());
    }

    @Override
    @Transactional
    public void saveTrips(List<Trip> trips) {
        tripJpaRepository.saveAll(trips.stream().map(this::toEntity).toList());
    }

    @Override
    @Transactional
    public void saveStopTimes(List<StopTime> stopTimes) {
        stopTimeJpaRepository.saveAll(stopTimes.stream().map(this::toEntity).toList());
    }

    private StopEntity toEntity(Stop stop) {
        Point location = GEOMETRY_FACTORY.createPoint(
                new org.locationtech.jts.geom.Coordinate(stop.coordinates().longitude(), stop.coordinates().latitude()));
        return new StopEntity(stop.id(), stop.name(), location);
    }

    private RouteEntity toEntity(Route route) {
        return new RouteEntity(route.id(), route.shortName(), route.longName(), route.mode());
    }

    private TripEntity toEntity(Trip trip) {
        return new TripEntity(trip.id(), trip.routeId(), trip.headsign());
    }

    private StopTimeEntity toEntity(StopTime stopTime) {
        return new StopTimeEntity(
                stopTime.tripId(), stopTime.stopId(), stopTime.stopSequence(), stopTime.arrival(), stopTime.departure());
    }
}
