package tn.transportview.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.transportview.infrastructure.persistence.entity.TripEntity;

public interface TripJpaRepository extends JpaRepository<TripEntity, String> {
}
