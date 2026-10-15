package tn.transportview.domain.port;

import java.util.List;
import java.util.Optional;
import tn.transportview.domain.model.Coordinates;
import tn.transportview.domain.model.Stop;

public interface StopRepositoryPort {

    Optional<Stop> findById(String stopId);

    List<Stop> findNearby(Coordinates origin, double radiusMeters);
}
