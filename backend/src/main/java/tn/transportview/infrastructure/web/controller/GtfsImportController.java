package tn.transportview.infrastructure.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.transportview.application.usecase.ImportGtfsFeedUseCase;
import tn.transportview.domain.model.ImportSummary;
import tn.transportview.infrastructure.web.advice.UnauthorizedException;
import tn.transportview.infrastructure.web.dto.ImportSummaryResponse;

/**
 * Admin-only trigger for importing a GTFS feed ZIP. The feed is uploaded as a file rather than
 * fetched by URL from this service, so the app never has to decide how to handle the upstream
 * portal's TLS issues (e.g. an expired certificate) — the operator downloads it out of band
 * and uploads the verified file here.
 */
@RestController
@RequestMapping("/api/v1/admin/gtfs")
public class GtfsImportController {

    private final ImportGtfsFeedUseCase importGtfsFeedUseCase;
    private final String adminApiKey;

    public GtfsImportController(
            ImportGtfsFeedUseCase importGtfsFeedUseCase,
            @Value("${admin.gtfs-import.api-key:}") String adminApiKey) {
        this.importGtfsFeedUseCase = importGtfsFeedUseCase;
        this.adminApiKey = adminApiKey;
    }

    @PostMapping("/import")
    public ImportSummaryResponse importFeed(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-Admin-Api-Key") String providedApiKey) {
        requireValidApiKey(providedApiKey);

        try (InputStream gtfsZip = file.getInputStream()) {
            ImportSummary summary = importGtfsFeedUseCase.execute(gtfsZip);
            return new ImportSummaryResponse(
                    summary.stopCount(), summary.routeCount(), summary.tripCount(), summary.stopTimeCount());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded GTFS file", e);
        }
    }

    private void requireValidApiKey(String providedApiKey) {
        if (adminApiKey.isBlank() || !constantTimeEquals(adminApiKey, providedApiKey)) {
            throw new UnauthorizedException("Invalid admin API key");
        }
    }

    private static boolean constantTimeEquals(String expected, String actual) {
        if (actual == null) {
            return false;
        }
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }
}
