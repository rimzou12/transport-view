package tn.transportview.infrastructure.gtfs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import tn.transportview.domain.model.GtfsFeedData;
import tn.transportview.domain.model.Route;
import tn.transportview.domain.model.Stop;
import tn.transportview.domain.model.StopTime;
import tn.transportview.domain.model.TransportMode;
import tn.transportview.domain.model.Trip;

class GtfsFeedParserTest {

    private final GtfsFeedParser parser = new GtfsFeedParser();

    @Test
    void parsesTheRealTransutSampleFeedKeepingOnlyMetroAndTgm() {
        GtfsFeedData feed;
        try (InputStream zip = getClass().getResourceAsStream("/gtfs/transtu-sample.zip")) {
            feed = parser.parse(zip);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThat(feed.routes()).extracting(Route::id).containsExactlyInAnyOrder("ligne1", "ligneTGM");
        assertThat(feed.routes()).allMatch(route -> route.mode() == TransportMode.METRO);

        assertThat(feed.trips()).extracting(Trip::id).containsExactlyInAnyOrder("ligne1010", "ligneTGM133");

        assertThat(feed.stops()).extracting(Stop::id)
                .contains("ben_arous", "echebbi_retour", "ennour_retour", "marsa_plage", "carthage_présidence")
                .doesNotContain("echebbi_retour\t");

        assertThat(feed.stopTimes()).extracting(StopTime::tripId)
                .containsOnly("ligne1010", "ligneTGM133");

        StopTime interpolatedMetroStop = feed.stopTimes().stream()
                .filter(st -> st.tripId().equals("ligne1010") && st.stopId().equals("echebbi_retour"))
                .findFirst()
                .orElseThrow();
        assertThat(interpolatedMetroStop.arrival()).isEqualTo(LocalTime.of(23, 37));
    }
}
