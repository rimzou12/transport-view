package tn.transportview.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import tn.transportview.domain.model.Coordinates;
import tn.transportview.domain.model.Stop;
import tn.transportview.domain.port.StopRepositoryPort;
import tn.transportview.infrastructure.persistence.entity.StopEntity;
import tn.transportview.infrastructure.persistence.repository.StopJpaRepository;

@Component
public class StopRepositoryAdapter implements StopRepositoryPort {

    private final StopJpaRepository stopJpaRepository;

    public StopRepositoryAdapter(StopJpaRepository stopJpaRepository) {
        this.stopJpaRepository = stopJpaRepository;
    }

    @Override
    public Optional<Stop> findById(String stopId) {
        return stopJpaRepository.findById(stopId).map(this::toDomain);
    }

    @Override
    public List<Stop> findNearby(Coordinates origin, double radiusMeters) {
        return stopJpaRepository.findWithinRadius(origin.latitude(), origin.longitude(), radiusMeters).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Stop> findByNameContaining(String query, int limit) {
        return stopJpaRepository.findByNameContainingIgnoreCase(query, Limit.of(limit)).stream()
                .map(this::toDomain)
                .toList();
    }

    private Stop toDomain(StopEntity entity) {
        return new Stop(
                entity.getId(),
                entity.getName(),
                new Coordinates(entity.getLocation().getY(), entity.getLocation().getX()));
    }
}
