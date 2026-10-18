package tn.transportview.infrastructure.gtfs;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import tn.transportview.domain.model.StopTime;

class GtfsStopTimeInterpolatorTest {

    @Test
    void leavesFullyTimedRowsUnchanged() {
        List<GtfsStopTimeRow> rows = List.of(
                new GtfsStopTimeRow("T1", "S1", 1, LocalTime.of(8, 0), LocalTime.of(8, 0)),
                new GtfsStopTimeRow("T1", "S2", 2, LocalTime.of(8, 10), LocalTime.of(8, 10)));

        List<StopTime> result = GtfsStopTimeInterpolator.interpolate(rows);

        assertThat(result).extracting(StopTime::stopId, StopTime::arrival)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("S1", LocalTime.of(8, 0)),
                        org.assertj.core.groups.Tuple.tuple("S2", LocalTime.of(8, 10)));
    }

    @Test
    void interpolatesEvenlyBetweenTwoTimedAnchors() {
        List<GtfsStopTimeRow> rows = List.of(
                new GtfsStopTimeRow("ligne1010", "ben_arous", 1, LocalTime.of(23, 33), LocalTime.of(23, 33)),
                new GtfsStopTimeRow("ligne1010", "echebbi_retour", 2, null, null),
                new GtfsStopTimeRow("ligne1010", "ennour_retour", 3, null, null),
                new GtfsStopTimeRow("ligne1010", "med_ali_retour", 4, LocalTime.of(23, 45), LocalTime.of(23, 45)));

        List<StopTime> result = GtfsStopTimeInterpolator.interpolate(rows);

        assertThat(result).hasSize(4);
        assertThat(result.get(1).stopId()).isEqualTo("echebbi_retour");
        assertThat(result.get(1).arrival()).isEqualTo(LocalTime.of(23, 37));
        assertThat(result.get(2).stopId()).isEqualTo("ennour_retour");
        assertThat(result.get(2).arrival()).isEqualTo(LocalTime.of(23, 41));
    }

    @Test
    void dropsLeadingRowsWithNoPriorAnchor() {
        List<GtfsStopTimeRow> rows = List.of(
                new GtfsStopTimeRow("T1", "S0", 1, null, null),
                new GtfsStopTimeRow("T1", "S1", 2, LocalTime.of(8, 0), LocalTime.of(8, 0)));

        List<StopTime> result = GtfsStopTimeInterpolator.interpolate(rows);

        assertThat(result).extracting(StopTime::stopId).containsExactly("S1");
    }

    @Test
    void dropsTrailingRowsWithNoFollowingAnchor() {
        List<GtfsStopTimeRow> rows = List.of(
                new GtfsStopTimeRow("T1", "S1", 1, LocalTime.of(8, 0), LocalTime.of(8, 0)),
                new GtfsStopTimeRow("T1", "S2", 2, null, null));

        List<StopTime> result = GtfsStopTimeInterpolator.interpolate(rows);

        assertThat(result).extracting(StopTime::stopId).containsExactly("S1");
    }

    @Test
    void dropsEverythingWhenNoAnchorExistsAtAll() {
        List<GtfsStopTimeRow> rows = List.of(
                new GtfsStopTimeRow("T1", "S1", 1, null, null),
                new GtfsStopTimeRow("T1", "S2", 2, null, null));

        List<StopTime> result = GtfsStopTimeInterpolator.interpolate(rows);

        assertThat(result).isEmpty();
    }
}
