package tn.transportview.domain.model;

import java.time.LocalTime;

public record StopTime(String tripId, String stopId, int stopSequence, LocalTime arrival, LocalTime departure) {
}
