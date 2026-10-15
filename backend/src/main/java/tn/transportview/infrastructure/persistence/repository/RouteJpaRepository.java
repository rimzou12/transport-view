package tn.transportview.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.transportview.infrastructure.persistence.entity.RouteEntity;

public interface RouteJpaRepository extends JpaRepository<RouteEntity, String> {
}
