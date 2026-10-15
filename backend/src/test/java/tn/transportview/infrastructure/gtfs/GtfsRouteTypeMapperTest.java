package tn.transportview.infrastructure.gtfs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tn.transportview.domain.model.TransportMode;

class GtfsRouteTypeMapperTest {

    @Test
    void mapsMetroRouteType() {
        assertThat(GtfsRouteTypeMapper.toTransportMode("1")).contains(TransportMode.METRO);
    }

    @Test
    void mapsTgmFerryRouteTypeToMetro() {
        assertThat(GtfsRouteTypeMapper.toTransportMode("4")).contains(TransportMode.METRO);
    }

    @Test
    void excludesBusRouteType() {
        assertThat(GtfsRouteTypeMapper.toTransportMode("3")).isEmpty();
    }

    @Test
    void excludesUnknownRouteType() {
        assertThat(GtfsRouteTypeMapper.toTransportMode("99")).isEmpty();
    }
}
