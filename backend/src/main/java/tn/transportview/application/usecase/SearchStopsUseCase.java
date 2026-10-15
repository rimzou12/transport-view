package tn.transportview.application.usecase;

import java.util.List;
import tn.transportview.domain.model.Stop;
import tn.transportview.domain.port.StopRepositoryPort;

public class SearchStopsUseCase {

    private static final int MAX_RESULTS = 10;

    private final StopRepositoryPort stopRepositoryPort;

    public SearchStopsUseCase(StopRepositoryPort stopRepositoryPort) {
        this.stopRepositoryPort = stopRepositoryPort;
    }

    public List<Stop> execute(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return stopRepositoryPort.findByNameContaining(query.trim(), MAX_RESULTS);
    }
}
