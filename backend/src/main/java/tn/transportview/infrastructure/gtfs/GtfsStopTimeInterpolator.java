package tn.transportview.infrastructure.gtfs;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import tn.transportview.domain.model.StopTime;

/**
 * GTFS allows blank arrival/departure on non-timepoint intermediate stops. This fills those
 * gaps by linear interpolation between the nearest timed stops (standard GTFS consumer practice).
 * Rows before the first or after the last known time in a trip cannot be bounded and are dropped.
 */
public final class GtfsStopTimeInterpolator {

    private GtfsStopTimeInterpolator() {
    }

    public static List<StopTime> interpolate(List<GtfsStopTimeRow> rowsOrderedBySequence) {
        List<LocalTime> resolvedTimes = resolveTimes(rowsOrderedBySequence);

        List<StopTime> result = new ArrayList<>();
        for (int i = 0; i < rowsOrderedBySequence.size(); i++) {
            LocalTime time = resolvedTimes.get(i);
            if (time == null) {
                continue;
            }
            GtfsStopTimeRow row = rowsOrderedBySequence.get(i);
            result.add(new StopTime(row.tripId(), row.stopId(), row.stopSequence(), time, time));
        }
        return result;
    }

    private static List<LocalTime> resolveTimes(List<GtfsStopTimeRow> rows) {
        List<LocalTime> times = new ArrayList<>(rows.size());
        for (GtfsStopTimeRow row : rows) {
            times.add(row.departure() != null ? row.departure() : row.arrival());
        }

        int previousAnchor = -1;
        for (int i = 0; i < times.size(); i++) {
            if (times.get(i) == null) {
                continue;
            }
            if (previousAnchor != -1 && i - previousAnchor > 1) {
                fillGap(times, previousAnchor, i);
            }
            previousAnchor = i;
        }
        return times;
    }

    private static void fillGap(List<LocalTime> times, int fromIndex, int toIndex) {
        LocalTime start = times.get(fromIndex);
        LocalTime end = times.get(toIndex);
        long totalSeconds = Duration.between(start, end).toSeconds();
        int steps = toIndex - fromIndex;

        for (int i = fromIndex + 1; i < toIndex; i++) {
            long offsetSeconds = totalSeconds * (i - fromIndex) / steps;
            times.set(i, start.plusSeconds(offsetSeconds));
        }
    }
}
