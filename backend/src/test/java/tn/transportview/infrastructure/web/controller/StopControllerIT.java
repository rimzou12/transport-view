package tn.transportview.infrastructure.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
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
class StopControllerIT {

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
    void findsStopsMatchingAnICaseInsensitiveSubstring() throws Exception {
        mockMvc.perform(get("/api/v1/stops").param("query", "marsa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Marsa Plage"));
    }

    @Test
    void returnsEmptyForABlankQuery() throws Exception {
        mockMvc.perform(get("/api/v1/stops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
