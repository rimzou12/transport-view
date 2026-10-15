package tn.transportview.infrastructure.persistence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.transportview.infrastructure.persistence.entity.StopEntity;

public interface StopJpaRepository extends JpaRepository<StopEntity, String> {

    @Query(value = """
            SELECT * FROM stops
            WHERE ST_DWithin(location, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, :radiusMeters)
            """, nativeQuery = true)
    List<StopEntity> findWithinRadius(@Param("lat") double lat, @Param("lon") double lon, @Param("radiusMeters") double radiusMeters);
}
