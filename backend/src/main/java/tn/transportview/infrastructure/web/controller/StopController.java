package tn.transportview.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.transportview.application.usecase.SearchStopsUseCase;
import tn.transportview.domain.model.Stop;
import tn.transportview.infrastructure.web.dto.StopResponse;

@Tag(name = "Stops", description = "Search imported stops by name, for journey-search autocomplete")
@RestController
@RequestMapping("/api/v1/stops")
public class StopController {

    private final SearchStopsUseCase searchStopsUseCase;

    public StopController(SearchStopsUseCase searchStopsUseCase) {
        this.searchStopsUseCase = searchStopsUseCase;
    }

    @Operation(summary = "Search stops by name (case-insensitive substring match)")
    @GetMapping
    public List<StopResponse> search(@RequestParam(required = false) String query) {
        return searchStopsUseCase.execute(query).stream().map(this::toResponse).toList();
    }

    private StopResponse toResponse(Stop stop) {
        return new StopResponse(stop.id(), stop.name(), stop.coordinates().latitude(), stop.coordinates().longitude());
    }
}
