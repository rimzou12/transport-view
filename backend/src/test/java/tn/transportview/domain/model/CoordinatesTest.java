package tn.transportview.domain.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class CoordinatesTest {

    @Test
    void acceptsValidTunisCoordinates() {
        assertThatCode(() -> new Coordinates(36.8065, 10.1815)).doesNotThrowAnyException();
    }

    @Test
    void rejectsOutOfRangeLatitude() {
        assertThatThrownBy(() -> new Coordinates(91, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsOutOfRangeLongitude() {
        assertThatThrownBy(() -> new Coordinates(36, 181))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
