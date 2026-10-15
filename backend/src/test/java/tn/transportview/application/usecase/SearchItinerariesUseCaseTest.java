package tn.transportview.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.transportview.domain.model.Coordinates;
import tn.transportview.domain.model.Itinerary;
import tn.transportview.domain.model.Leg;
import tn.transportview.domain.model.Stop;
import tn.transportview.domain.model.TransportMode;
import tn.transportview.domain.routing.ItineraryRouter;
import tn.transportview.domain.routing.JourneyRequest;

@ExtendWith(MockitoExtension.class)
class SearchItinerariesUseCaseTest {

    @Mock
    private ItineraryRouter itineraryRouter;

    @Test
    void delegatesSearchToItineraryRouter() {
        JourneyRequest request = new JourneyRequest(
                new Coordinates(36.8008, 10.1815),
                new Coordinates(36.8325, 10.2458),
                LocalTime.of(8, 0));

        Stop origin = new Stop("STOP_TM", "Tunis Marine", request.origin());
        Stop destination = new Stop("STOP_LB", "Les Berges du Lac", request.destination());
        Itinerary expected = new Itinerary(List.of(
                new Leg(TransportMode.METRO, origin, destination, LocalTime.of(8, 0), LocalTime.of(8, 20), "M2")));

        when(itineraryRouter.search(request)).thenReturn(List.of(expected));

        SearchItinerariesUseCase useCase = new SearchItinerariesUseCase(itineraryRouter);
        List<Itinerary> result = useCase.execute(request);

        assertThat(result).containsExactly(expected);
        verify(itineraryRouter).search(request);
    }
}
