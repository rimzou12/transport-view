package tn.transportview.infrastructure.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import tn.transportview.TestcontainersConfiguration;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "admin.gtfs-import.api-key=test-admin-key")
class ItineraryControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void importSampleFeed() throws Exception {
        try (InputStream zip = getClass().getResourceAsStream("/gtfs/transtu-sample.zip")) {
            MockMultipartFile file = new MockMultipartFile("file", "transtu-sample.zip", "application/zip", zip.readAllBytes());
            mockMvc.perform(multipart("/api/v1/admin/gtfs/import").file(file).header("X-Admin-Api-Key", "test-admin-key"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void findsARealTgmJourneyFromImportedData() throws Exception {
        // marsa_plage -> carthage_présidence, served by trip ligneTGM133 (departs 23:45).
        mockMvc.perform(post("/api/v1/itineraries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originLatitude": 36.87917,
                                  "originLongitude": 10.32500,
                                  "destinationLatitude": 36.85556,
                                  "destinationLongitude": 10.32194,
                                  "departureTime": "23:30:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].legs[0].mode").value("METRO"))
                .andExpect(jsonPath("$[0].legs[0].routeShortName").value("ligne TGM"));
    }

    @Test
    void rejectsAMalformedDepartureTime() throws Exception {
        mockMvc.perform(post("/api/v1/itineraries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originLatitude": 36.87917,
                                  "originLongitude": 10.32500,
                                  "destinationLatitude": 36.85556,
                                  "destinationLongitude": 10.32194,
                                  "departureTime": "not-a-time"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsOriginAndDestinationTooFarApart() throws Exception {
        mockMvc.perform(post("/api/v1/itineraries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originLatitude": 36.8008,
                                  "originLongitude": 10.1815,
                                  "destinationLatitude": 48.8566,
                                  "destinationLongitude": 2.3522
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/itineraries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
