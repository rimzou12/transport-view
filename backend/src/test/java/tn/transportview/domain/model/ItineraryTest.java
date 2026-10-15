package tn.transportview.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ItineraryTest {

    private final Stop tunisMarine = new Stop("STOP_TM", "Tunis Marine", new Coordinates(36.8008, 10.1815));
    private final Stop lesBerges = new Stop("STOP_LB", "Les Berges du Lac", new Coordinates(36.8325, 10.2458));

    @Test
    void transferCountIsLegCountMinusOne() {
        Leg leg1 = new Leg(TransportMode.METRO, tunisMarine, lesBerges, LocalTime.of(8, 0), LocalTime.of(8, 20), "M2");
        Itinerary itinerary = new Itinerary(List.of(leg1));

        assertThat(itinerary.transferCount()).isZero();
    }

    @Test
    void totalDurationSpansFirstDepartureToLastArrival() {
        Leg leg1 = new Leg(TransportMode.BUS, tunisMarine, lesBerges, LocalTime.of(8, 0), LocalTime.of(8, 15), "20");
        Leg leg2 = new Leg(TransportMode.METRO, lesBerges, tunisMarine, LocalTime.of(8, 20), LocalTime.of(8, 40), "M2");
        Itinerary itinerary = new Itinerary(List.of(leg1, leg2));

        assertThat(itinerary.totalDuration().toMinutes()).isEqualTo(40);
        assertThat(itinerary.transferCount()).isEqualTo(1);
    }

    @Test
    void rejectsEmptyLegs() {
        assertThatThrownBy(() -> new Itinerary(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
