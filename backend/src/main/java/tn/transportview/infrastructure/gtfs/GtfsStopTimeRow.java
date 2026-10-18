package tn.transportview.infrastructure.gtfs;

import java.time.LocalTime;

/**
 * Raw stop_times.txt row, pre-interpolation. {@code arrival}/{@code departure} are null
 * when the GTFS source left the field blank (common for non-timepoint intermediate stops).
 */
public record GtfsStopTimeRow(String tripId, String stopId, int stopSequence, LocalTime arrival, LocalTime departure) {
}
