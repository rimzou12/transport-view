package tn.transportview.domain.model;

import java.time.Duration;
import java.util.List;

public record Itinerary(List<Leg> legs) {

    public Itinerary {
        if (legs == null || legs.isEmpty()) {
            throw new IllegalArgumentException("an itinerary must contain at least one leg");
        }
        legs = List.copyOf(legs);
    }

    public int transferCount() {
        return legs.size() - 1;
    }

    public Duration totalDuration() {
        return Duration.between(legs.get(0).departure(), legs.get(legs.size() - 1).arrival());
    }
}
