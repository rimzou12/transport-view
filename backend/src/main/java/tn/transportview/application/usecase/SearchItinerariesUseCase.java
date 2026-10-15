package tn.transportview.application.usecase;

import java.util.List;
import tn.transportview.domain.model.Itinerary;
import tn.transportview.domain.routing.ItineraryRouter;
import tn.transportview.domain.routing.JourneyRequest;

public class SearchItinerariesUseCase {

    private final ItineraryRouter itineraryRouter;

    public SearchItinerariesUseCase(ItineraryRouter itineraryRouter) {
        this.itineraryRouter = itineraryRouter;
    }

    public List<Itinerary> execute(JourneyRequest request) {
        return itineraryRouter.search(request);
    }
}
