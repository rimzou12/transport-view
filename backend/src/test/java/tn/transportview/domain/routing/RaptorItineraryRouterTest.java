package tn.transportview.domain.routing;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import tn.transportview.domain.model.Coordinates;
import tn.transportview.domain.model.Itinerary;
import tn.transportview.domain.model.Route;
import tn.transportview.domain.model.Stop;
import tn.transportview.domain.model.StopTime;
import tn.transportview.domain.model.TransportMode;
import tn.transportview.domain.model.Trip;

class RaptorItineraryRouterTest {

    private final Stop stopA = new Stop("A", "Stop A", new Coordinates(36.800, 10.180));
    private final Stop stopB = new Stop("B", "Stop B", new Coordinates(36.810, 10.190));
    private final Stop stopC = new Stop("C", "Stop C", new Coordinates(36.820, 10.200));
    private final Stop stopD = new Stop("D", "Stop D", new Coordinates(36.830, 10.210));

    @Test
    void findsADirectSingleLegTrip() {
        Route metro = new Route("R1", "M1", "Line 1", TransportMode.METRO);
        Trip trip = new Trip("T1", "R1", "Towards B");
        List<StopTime> stopTimes = List.of(
                new StopTime("T1", "A", 1, LocalTime.of(8, 0), LocalTime.of(8, 0)),
                new StopTime("T1", "B", 2, LocalTime.of(8, 10), LocalTime.of(8, 10)));

        RaptorItineraryRouter router = routerWith(List.of(stopA, stopB), stopTimes, List.of(trip), List.of(metro));

        JourneyRequest request = new JourneyRequest(stopA.coordinates(), stopB.coordinates(), LocalTime.of(7, 55));
        List<Itinerary> results = router.search(request);

        assertThat(results).hasSize(1);
        Itinerary itinerary = results.get(0);
        assertThat(itinerary.legs()).hasSize(1);
        assertThat(itinerary.legs().get(0).mode()).isEqualTo(TransportMode.METRO);
        assertThat(itinerary.legs().get(0).fromStop().id()).isEqualTo("A");
        assertThat(itinerary.legs().get(0).toStop().id()).isEqualTo("B");
        assertThat(itinerary.legs().get(0).arrival()).isEqualTo(LocalTime.of(8, 10));
        assertThat(itinerary.transferCount()).isZero();
    }

    @Test
    void findsAMultiLegTripWithOneTransfer() {
        Route metro = new Route("R1", "M1", "Line 1", TransportMode.METRO);
        Route tgm = new Route("R2", "TGM", "TGM Line", TransportMode.METRO);
        Trip tripAB = new Trip("T1", "R1", "Towards B");
        Trip tripBC = new Trip("T2", "R2", "Towards C");
        List<StopTime> stopTimes = List.of(
                new StopTime("T1", "A", 1, LocalTime.of(8, 0), LocalTime.of(8, 0)),
                new StopTime("T1", "B", 2, LocalTime.of(8, 10), LocalTime.of(8, 10)),
                new StopTime("T2", "B", 1, LocalTime.of(8, 15), LocalTime.of(8, 15)),
                new StopTime("T2", "C", 2, LocalTime.of(8, 25), LocalTime.of(8, 25)));

        RaptorItineraryRouter router = routerWith(
                List.of(stopA, stopB, stopC), stopTimes, List.of(tripAB, tripBC), List.of(metro, tgm));

        JourneyRequest request = new JourneyRequest(stopA.coordinates(), stopC.coordinates(), LocalTime.of(7, 55));
        List<Itinerary> results = router.search(request);

        assertThat(results).hasSize(1);
        Itinerary itinerary = results.get(0);
        assertThat(itinerary.legs()).hasSize(2);
        assertThat(itinerary.legs().get(0).toStop().id()).isEqualTo("B");
        assertThat(itinerary.legs().get(1).fromStop().id()).isEqualTo("B");
        assertThat(itinerary.legs().get(1).toStop().id()).isEqualTo("C");
        assertThat(itinerary.transferCount()).isEqualTo(1);
        assertThat(itinerary.legs().get(1).arrival()).isEqualTo(LocalTime.of(8, 25));
    }

    @Test
    void fallsBackToTaxiWhenStopsAreDisconnected() {
        Route metro = new Route("R1", "M1", "Line 1", TransportMode.METRO);
        Trip tripAB = new Trip("T1", "R1", "Towards B");
        // Only A->B is served; nothing connects onward to C.
        List<StopTime> stopTimes = List.of(
                new StopTime("T1", "A", 1, LocalTime.of(8, 0), LocalTime.of(8, 0)),
                new StopTime("T1", "B", 2, LocalTime.of(8, 10), LocalTime.of(8, 10)));

        RaptorItineraryRouter router = routerWith(
                List.of(stopA, stopB, stopC), stopTimes, List.of(tripAB), List.of(metro));

        JourneyRequest request = new JourneyRequest(stopA.coordinates(), stopC.coordinates(), LocalTime.of(7, 55));
        List<Itinerary> results = router.search(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).legs()).hasSize(1);
        assertThat(results.get(0).legs().get(0).mode()).isEqualTo(TransportMode.TAXI);
    }

    @Test
    void prefersTheFewerTransferOptionWhenArrivalTimesTie() {
        Route direct = new Route("R1", "D1", "Direct Line", TransportMode.METRO);
        Route legA = new Route("R2", "L2a", "Feeder A", TransportMode.METRO);
        Route legB = new Route("R3", "L2b", "Feeder B", TransportMode.METRO);
        Trip directTrip = new Trip("T1", "R1", "Towards D");
        Trip transferTripAB = new Trip("T2", "R2", "Towards B");
        Trip transferTripBD = new Trip("T3", "R3", "Towards D");

        List<StopTime> stopTimes = List.of(
                // Direct A -> D, arriving 08:20, zero transfers.
                new StopTime("T1", "A", 1, LocalTime.of(8, 0), LocalTime.of(8, 0)),
                new StopTime("T1", "D", 2, LocalTime.of(8, 20), LocalTime.of(8, 20)),
                // Transfer path A -> B -> D, also arriving 08:20, one transfer.
                new StopTime("T2", "A", 1, LocalTime.of(8, 0), LocalTime.of(8, 0)),
                new StopTime("T2", "B", 2, LocalTime.of(8, 5), LocalTime.of(8, 5)),
                new StopTime("T3", "B", 1, LocalTime.of(8, 10), LocalTime.of(8, 10)),
                new StopTime("T3", "D", 2, LocalTime.of(8, 20), LocalTime.of(8, 20)));

        RaptorItineraryRouter router = routerWith(
                List.of(stopA, stopB, stopD),
                stopTimes,
                List.of(directTrip, transferTripAB, transferTripBD),
                List.of(direct, legA, legB));

        JourneyRequest request = new JourneyRequest(stopA.coordinates(), stopD.coordinates(), LocalTime.of(7, 55));
        List<Itinerary> results = router.search(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).transferCount()).isZero();
        assertThat(results.get(0).legs().get(0).arrival()).isEqualTo(LocalTime.of(8, 20));
    }

    private RaptorItineraryRouter routerWith(List<Stop> stops, List<StopTime> stopTimes, List<Trip> trips, List<Route> routes) {
        return new RaptorItineraryRouter(
                new InMemoryStopRepository(stops), new InMemoryTransitScheduleRepository(stopTimes, trips, routes));
    }
}
