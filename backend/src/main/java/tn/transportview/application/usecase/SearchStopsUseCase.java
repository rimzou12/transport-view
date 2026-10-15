package tn.transportview.application.usecase;

import java.util.List;
import tn.transportview.domain.model.Stop;
import tn.transportview.domain.port.StopRepositoryPort;

public class SearchStopsUseCase {

    private static final int MAX_RESULTS = 30;

    private final StopRepositoryPort stopRepositoryPort;

    public SearchStopsUseCase(StopRepositoryPort stopRepositoryPort) {
        this.stopRepositoryPort = stopRepositoryPort;
    }

    /**
     * A blank query browses all stops (up to {@link #MAX_RESULTS}) rather than returning
     * nothing — lets the frontend show the full list on focus, before the user types anything,
     * so they can see what's actually available instead of guessing a name that doesn't exist.
     */
    public List<Stop> execute(String query) {
        String normalized = query == null ? "" : query.trim();
        return stopRepositoryPort.findByNameContaining(normalized, MAX_RESULTS);
    }
}
