package tn.transportview.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import tn.transportview.TestcontainersConfiguration;
import tn.transportview.domain.model.Coordinates;
import tn.transportview.domain.model.Stop;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, StopRepositoryAdapter.class})
@Sql(statements = {
        "CREATE EXTENSION IF NOT EXISTS postgis",
        "INSERT INTO stops (id, name, location) VALUES ('STOP_TM', 'Tunis Marine', "
                + "ST_SetSRID(ST_MakePoint(10.1815, 36.8008), 4326)::geography)"
})
class StopRepositoryAdapterIT {

    @Autowired
    private StopRepositoryAdapter stopRepositoryAdapter;

    @Test
    void findsStopsWithinRadiusOfOrigin() {
        Coordinates nearTunisMarine = new Coordinates(36.8010, 10.1816);

        var nearbyStops = stopRepositoryAdapter.findNearby(nearTunisMarine, 500);

        assertThat(nearbyStops).extracting(Stop::id).contains("STOP_TM");
    }

    @Test
    void returnsEmptyWhenNoStopWithinRadius() {
        Coordinates farFromAnyStop = new Coordinates(10.0, 10.0);

        var nearbyStops = stopRepositoryAdapter.findNearby(farFromAnyStop, 500);

        assertThat(nearbyStops).isEmpty();
    }
}
