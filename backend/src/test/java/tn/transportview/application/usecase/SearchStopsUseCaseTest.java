package tn.transportview.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.transportview.domain.model.Coordinates;
import tn.transportview.domain.model.Stop;
import tn.transportview.domain.port.StopRepositoryPort;

@ExtendWith(MockitoExtension.class)
class SearchStopsUseCaseTest {

    @Mock
    private StopRepositoryPort stopRepositoryPort;

    @Test
    void delegatesToRepositoryWithATrimmedQuery() {
        Stop stop = new Stop("STOP_1", "Tunis Marine", new Coordinates(36.8, 10.18));
        when(stopRepositoryPort.findByNameContaining("marine", 30)).thenReturn(List.of(stop));

        SearchStopsUseCase useCase = new SearchStopsUseCase(stopRepositoryPort);
        List<Stop> result = useCase.execute("  marine  ");

        assertThat(result).containsExactly(stop);
    }

    @Test
    void browsesAllStopsForABlankOrNullQuery() {
        Stop stop = new Stop("STOP_1", "Tunis Marine", new Coordinates(36.8, 10.18));
        when(stopRepositoryPort.findByNameContaining("", 30)).thenReturn(List.of(stop));

        SearchStopsUseCase useCase = new SearchStopsUseCase(stopRepositoryPort);

        assertThat(useCase.execute("   ")).containsExactly(stop);
        assertThat(useCase.execute(null)).containsExactly(stop);
    }
}
