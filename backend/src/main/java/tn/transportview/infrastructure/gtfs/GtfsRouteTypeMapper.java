package tn.transportview.infrastructure.gtfs;

import java.util.Optional;
import tn.transportview.domain.model.TransportMode;

/**
 * Maps GTFS {@code route_type} codes to our supported {@link TransportMode}s.
 * Only metro (1) and TGM (4 — GTFS models it as "ferry", but it's Transtu's light-rail
 * line to La Marsa) are supported for now; bus (3) and everything else is filtered out
 * because the TRANSTU feed does not have usable bus schedule data (see issue #7).
 */
public final class GtfsRouteTypeMapper {

    private static final String METRO_ROUTE_TYPE = "1";
    private static final String TGM_ROUTE_TYPE = "4";

    private GtfsRouteTypeMapper() {
    }

    public static Optional<TransportMode> toTransportMode(String gtfsRouteType) {
        return switch (gtfsRouteType) {
            case METRO_ROUTE_TYPE, TGM_ROUTE_TYPE -> Optional.of(TransportMode.METRO);
            default -> Optional.empty();
        };
    }
}
