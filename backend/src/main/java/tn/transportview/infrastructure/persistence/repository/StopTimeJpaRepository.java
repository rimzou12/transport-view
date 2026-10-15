package tn.transportview.infrastructure.persistence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.transportview.infrastructure.persistence.entity.StopTimeEntity;
import tn.transportview.infrastructure.persistence.entity.StopTimeId;

public interface StopTimeJpaRepository extends JpaRepository<StopTimeEntity, StopTimeId> {

    List<StopTimeEntity> findByStopIdOrderByIdStopSequence(String stopId);

    List<StopTimeEntity> findByIdTripIdOrderByIdStopSequence(String tripId);
}
