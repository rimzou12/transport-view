package tn.transportview.infrastructure.persistence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.transportview.infrastructure.persistence.entity.StopTimeEntity;

public interface StopTimeJpaRepository extends JpaRepository<StopTimeEntity, Long> {

    List<StopTimeEntity> findByStopIdOrderByStopSequence(String stopId);

    List<StopTimeEntity> findByTripIdOrderByStopSequence(String tripId);
}
