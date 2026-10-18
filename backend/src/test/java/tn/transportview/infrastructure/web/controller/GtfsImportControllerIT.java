package tn.transportview.infrastructure.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import tn.transportview.TestcontainersConfiguration;
import tn.transportview.infrastructure.persistence.repository.RouteJpaRepository;
import tn.transportview.infrastructure.persistence.repository.StopJpaRepository;
import tn.transportview.infrastructure.persistence.repository.StopTimeJpaRepository;
import tn.transportview.infrastructure.persistence.repository.TripJpaRepository;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "admin.gtfs-import.api-key=test-admin-key")
class GtfsImportControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StopJpaRepository stopJpaRepository;

    @Autowired
    private RouteJpaRepository routeJpaRepository;

    @Autowired
    private TripJpaRepository tripJpaRepository;

    @Autowired
    private StopTimeJpaRepository stopTimeJpaRepository;

    @Test
    void rejectsImportWithoutAValidApiKey() throws Exception {
        mockMvc.perform(multipart("/api/v1/admin/gtfs/import")
                        .file(sampleFeedFile())
                        .header("X-Admin-Api-Key", "wrong-key"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void importsTheSampleFeedAndIsIdempotentOnReRun() throws Exception {
        mockMvc.perform(multipart("/api/v1/admin/gtfs/import")
                        .file(sampleFeedFile())
                        .header("X-Admin-Api-Key", "test-admin-key"))
                .andExpect(status().isOk());

        long firstStopCount = stopJpaRepository.count();
        long firstRouteCount = routeJpaRepository.count();
        long firstTripCount = tripJpaRepository.count();
        long firstStopTimeCount = stopTimeJpaRepository.count();

        assertThat(firstRouteCount).isEqualTo(2);
        assertThat(firstTripCount).isEqualTo(2);
        assertThat(routeJpaRepository.findById("ligne28")).isEmpty();

        mockMvc.perform(multipart("/api/v1/admin/gtfs/import")
                        .file(sampleFeedFile())
                        .header("X-Admin-Api-Key", "test-admin-key"))
                .andExpect(status().isOk());

        assertThat(stopJpaRepository.count()).isEqualTo(firstStopCount);
        assertThat(routeJpaRepository.count()).isEqualTo(firstRouteCount);
        assertThat(tripJpaRepository.count()).isEqualTo(firstTripCount);
        assertThat(stopTimeJpaRepository.count()).isEqualTo(firstStopTimeCount);
    }

    private MockMultipartFile sampleFeedFile() throws Exception {
        try (InputStream zip = getClass().getResourceAsStream("/gtfs/transtu-sample.zip")) {
            return new MockMultipartFile("file", "transtu-sample.zip", "application/zip", zip.readAllBytes());
        }
    }
}
