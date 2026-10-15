package tn.transportview.domain.model;

import java.time.LocalTime;

public record Leg(
        TransportMode mode,
        Stop fromStop,
        Stop toStop,
        LocalTime departure,
        LocalTime arrival,
        String routeShortName) {
}
